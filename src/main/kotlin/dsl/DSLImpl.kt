package dsl

import kotlin.reflect.KType
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

    override fun build(): MAS<Belief, Goal, Env> {
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
        builder.apply(block)
    }

    override fun build(): Agent<Belief, Goal, Env> {
        //TODO maybe some consistency checks
        return AgentImpl(initialBeliefs, initialGoals, beliefPlans, goalPlans)
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
    override val adding: TriggerBuilder.Addition<Belief, Goal, Env>
        get() = TriggerAdditionImpl(beliefPlans, goalPlans)
    override val removing: TriggerBuilder.Removal<Belief, Goal, Env>
        get() = TriggerRemovalImpl(beliefPlans, goalPlans)
    override val failing: TriggerBuilder.FailureInterception<Belief, Goal, Env>
        get() = TriggerFailureInterceptionImpl(goalPlans)
}

//TODO lot of duplicated code... how can we fix this?

private class TriggerAdditionImpl<Belief: Any, Goal: Any, Env: Environment>(
    val beliefPlans: MutableCollection<Plan.Belief<Belief, Goal, Env, *, *>>,
    val goalPlans: MutableCollection<Plan.Goal<Belief, Goal, Env, *, *>>
) : TriggerBuilder.Addition<Belief, Goal, Env> {
    override fun <Context : Any> belief(beliefQuery: Belief.() -> Context?): PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> =
        BeliefAdditionPlanBuilderImpl(beliefPlans, beliefQuery)

    override fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.Addition.Goal<Belief, Goal, Env, Context> =
        GoalAdditionPlanBuilderImpl(goalPlans, goalQuery)
}

private class TriggerRemovalImpl<Belief: Any, Goal: Any, Env: Environment>(
    val beliefPlans: MutableCollection<Plan.Belief<Belief, Goal, Env, *, *>>,
    val goalPlans: MutableCollection<Plan.Goal<Belief, Goal, Env, *, *>>
) : TriggerBuilder.Removal<Belief, Goal, Env> {
    override fun <Context : Any> belief(beliefQuery: Belief.() -> Context?): PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> =
        BeliefRemovalPlanBuilderImpl(beliefPlans, beliefQuery)

    override fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.Removal.Goal<Belief, Goal, Env, Context> =
        GoalRemovalPlanBuilderImpl(goalPlans, goalQuery)
}

private class TriggerFailureInterceptionImpl<Belief: Any, Goal: Any, Env: Environment>(
    val goalPlans: MutableCollection<Plan.Goal<Belief, Goal, Env, *, *>>
) : TriggerBuilder.FailureInterception<Belief, Goal, Env> {
    override fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context> =
        GoalFailurePlanBuilderImpl(goalPlans, goalQuery)
}


private class BeliefAdditionPlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val beliefPlans: MutableCollection<Plan.Belief<Belief, Goal, Env, *, *>>,
    val trigger: Belief.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
    : PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> {
        this.guard = guard
        return this
    }

    override fun <PlanResult> triggers(
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
        resultType: KType
    ) : Plan.Belief.Addition<Belief, Goal, Env, Context, PlanResult> {
        val plan = BeliefAdditionPlan(trigger, guard, body, resultType)
        beliefPlans += plan
        return plan
    }
}

private class GoalAdditionPlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val goalPlans: MutableCollection<Plan.Goal<Belief, Goal, Env, *, *>>,
    val trigger: Goal.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.Addition.Goal<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
            : PlanBuilder.Addition.Goal<Belief, Goal, Env, Context> {
        this.guard = guard
        return this
    }

    override fun <PlanResult> triggers(
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
        resultType: KType
    ) : Plan.Goal.Addition<Belief, Goal, Env, Context, PlanResult> {
        val plan = GoalAdditionPlan(trigger, guard, body, resultType)
        goalPlans += plan
        return plan
    }
}

private class BeliefRemovalPlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val beliefPlans: MutableCollection<Plan.Belief<Belief, Goal, Env, *, *>>,
    val trigger: Belief.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
            : PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> {
        this.guard = guard
        return this
    }

    override fun <PlanResult> triggers(
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
        resultType: KType
    ) : Plan.Belief.Removal<Belief, Goal, Env, Context, PlanResult> {
        val plan = BeliefRemovalPlan(trigger, guard, body, resultType)
        beliefPlans += plan
        return plan
    }
}

private class GoalRemovalPlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val goalPlans: MutableCollection<Plan.Goal<Belief, Goal, Env, *, *>>,
    val trigger: Goal.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.Removal.Goal<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
            : PlanBuilder.Removal.Goal<Belief, Goal, Env, Context> {
        this.guard = guard
        return this
    }

    override fun <PlanResult> triggers(
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
        resultType: KType
    ) : Plan.Goal.Removal<Belief, Goal, Env, Context, PlanResult> {
        val plan = GoalRemovalPlan(trigger, guard, body, resultType)
        goalPlans += plan
        return plan
    }
}

private class GoalFailurePlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val goalPlans: MutableCollection<Plan.Goal<Belief, Goal, Env, *, *>>,
    val trigger: Goal.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
            : PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context> {
        this.guard = guard
        return this
    }

    override fun <PlanResult> triggers(
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
        resultType: KType
    ) : Plan.Goal.Failure<Belief, Goal, Env, Context, PlanResult> {
        val plan = GoalFailurePlan(trigger, guard, body, resultType)
        goalPlans += plan
        return plan
    }
}

//////////////////////////////////////////////////////////////////////
// ENTRYPOINT
//////////////////////////////////////////////////////////////////////

@JaktaDSL
fun <Belief : Any, Goal : Any, Env : Environment> mas(
    block: MasBuilder<Belief, Goal, Env>.() -> Unit
): MAS<Belief, Goal, Env> {
    val mb = MasBuilderImpl<Belief, Goal, Env>()
    mb.apply(block)
    return mb.build()
}

//TODO I want more entrypoints, to be able to define stuff in multiple files..
// this should be easy, we can do it later

