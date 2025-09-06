package dsl

import kotlin.reflect.typeOf

// ---------- Builders (DSL) ----------

private class MasBuilderImpl<Belief : Any, Goal : Any, Env : Environment> :
    MasBuilder<Belief, Goal, Env> {

    private var environment: Env? = null
    private val agents = mutableListOf<Agent<Belief, Goal, Env>>()

    override fun agent(
        block: AgentBuilder<Belief, Goal, Env>.() -> Unit
    ): Agent<Belief, Goal, Env> {
        val agentBuilder = AgentBuilderImpl<Belief, Goal, Env>()
        val agent = agentBuilder.apply(block).build()
        agents += agent
        return agent
    }

    override fun environment(block: () -> Env) {
        environment = block()
    }

    fun build(): MAS<Belief, Goal, Env> {
        val env = environment ?: throw IllegalStateException("Must provide an Environment for the MAS")
        return MASImpl(env, agents.toSet())
    }
}

private class AgentBuilderImpl<Belief : Any, Goal : Any, Env : Environment>() : AgentBuilder<Belief, Goal, Env> {

    private val initialBeliefs = mutableListOf<Belief>()
    private val initialGoals = mutableListOf<Goal>()
    private val beliefPlans = mutableListOf<Plan.Belief<Belief, Goal, Env, *, *>>()
    private val goalPlans = mutableListOf<Plan.Goal<Belief, Goal, Env, *, *>>()

    override fun believes(block: BeliefBuilder<Belief>.() -> Unit) {
        val builder = BeliefBuilderImpl(initialBeliefs)
        builder.apply(block)
    }

    override fun hasInitialGoals(block: GoalBuilder<Goal>.() -> Unit) {
        val builder = GoalBuilderImpl(initialGoals)
        builder.apply(block)
    }

    override fun hasPlans(block: PlanLibraryBuilder<Belief, Goal, Env>.() -> Unit) {
        val builder = PlanLibraryBuilderImpl(beliefPlans, goalPlans)
        block(builder)
    }

    fun build(): Agent<Belief, Goal, Env> {
        //TODO maybe some consistency checks
        return AgentImpl(initialBeliefs, beliefPlans, goalPlans)
    }
}

private class BeliefBuilderImpl<Belief : Any>(
    private val sink: MutableCollection<Belief>
) : BeliefBuilder<Belief> {
    override operator fun Belief.unaryPlus() {
        sink += this
    }
}

private class GoalBuilderImpl<Goal : Any>(
    private val sink: MutableCollection<Goal>
) : GoalBuilder<Goal> {
    override operator fun Goal.not() {
        sink += this
    }
}

private class PlanLibraryBuilderImpl<Belief: Any, Goal: Any, Env: Environment>(
    val beliefPlans: MutableCollection<Plan.Belief<Belief, Goal, Env, *, *>>,
    val goalPlans: MutableCollection<Plan.Goal<Belief, Goal, Env, *, *>>
) : PlanLibraryBuilder<Belief, Goal, Env> {
    override val adding: TriggerBuilder.Addition<Belief, Goal, Env> =  TriggerAdditionImpl(beliefPlans, goalPlans)
    override val removing: TriggerBuilder.Removal<Belief, Goal, Env> =  TriggerRemovalImpl(beliefPlans, goalPlans)
    override val failing: TriggerBuilder.FailureInterception<Belief, Goal, Env> = TriggerFailureInterceptionImpl(goalPlans)
}


private class TriggerAdditionImpl<Belief: Any, Goal: Any, Env: Environment>(
    val beliefPlans: MutableCollection<Plan.Belief<Belief, Goal, Env, *, *>>,
    val goalPlans: MutableCollection<Plan.Goal<Belief, Goal, Env, *, *>>
) : TriggerBuilder.Addition<Belief, Goal, Env> {
    override fun <Context : Any> belief(beliefQuery: Belief.() -> Context?): PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> {
        val builder = BeliefAdditionPlanBuilderImpl<Belief, Goal, Env, Context>(beliefQuery)
        val plan = builder.build()
        beliefPlans += plan
    }

    override fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.Addition.Goal<Belief, Goal, Env, Context> {
        val plan = TODO()
        goalPlans += plan
    }
}

private class TriggerRemovalImpl<Belief: Any, Goal: Any, Env: Environment>(
    val beliefPlans: MutableCollection<Plan.Belief<Belief, Goal, Env, *, *>>,
    val goalPlans: MutableCollection<Plan.Goal<Belief, Goal, Env, *, *>>
) : TriggerBuilder.Removal<Belief, Goal, Env> {
    override fun <Context : Any> belief(beliefQuery: Belief.() -> Context?): PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> {
        val plan = TODO()
        beliefPlans += plan
    }

    override fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.Removal.Goal<Belief, Goal, Env, Context> {
        val plan = TODO()
        goalPlans += plan
    }
}


private class TriggerFailureInterceptionImpl<Belief: Any, Goal: Any, Env: Environment>(
    val goalPlans: MutableCollection<Plan.Goal<Belief, Goal, Env, *, *>>
) : TriggerBuilder.FailureInterception<Belief, Goal, Env> {
    override fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context> {
        val plan = TODO()
        goalPlans += plan
    }
}


private class BeliefAdditionPlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val trigger: Belief.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> {

    var plan: Plan.Belief.Addition<Belief, Goal, Env, Context, *>? = null

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?):
            PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> {
        this.guard = guard
        return this
    }

    override fun <PlanResult> triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult):
            Plan.Belief.Addition<Belief, Goal, Env, Context, PlanResult> {
        val plan = BeliefAdditionPlan(trigger, guard, body)
        this.plan = plan
        return plan //TODO this is strange
    }

    fun build() : Plan.Belief.Addition<Belief, Goal, Env, Context, *> {
        return plan ?: throw IllegalStateException("Unable to create plan") //TODO check
    }
}


//////////////////////////////////////////////////////////////////////
// ENTRYPOINT
//////////////////////////////////////////////////////////////////////

@JaktaDSL
fun <Belief : Any, Goal : Any, Env : Environment> mas(
    block: MasBuilder<Belief, Goal, Env>.() -> Unit
): MAS<Belief, Goal, Env> {
//    val mb = MasBuilderImpl<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>()
//    mb.apply(block)
//    return mb.build()
    return TODO()
}

//TODO I want more entrypoints, to be able to define stuff in multiple files..
// this should be easy, we can do it later

