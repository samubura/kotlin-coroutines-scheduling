package dsl

import kotlin.reflect.KType

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

    private var initialBeliefs = listOf<Belief>()
    private var initialGoals = listOf<Goal>()
    private var beliefPlans = listOf<Plan.Belief<Belief, Goal, Env, *, *>>()
    private var goalPlans = listOf<Plan.Goal<Belief, Goal, Env, *, *>>()

    override fun believes(block: BeliefBuilder<Belief>.() -> Unit) {
        val builder = BeliefBuilderImpl(::addBelief)
        builder.apply(block)
    }

    override fun hasInitialGoals(block: GoalBuilder<Goal>.() -> Unit) {
        val builder = GoalBuilderImpl(::addGoal)
        builder.apply(block)
    }

    override fun hasPlans(block: PlanLibraryBuilder<Belief, Goal, Env>.() -> Unit) {
        val builder = PlanLibraryBuilderImpl(::addBeliefPlan, ::addGoalPlan)
        builder.apply(block)
    }

    override fun addBelief(belief: Belief) { initialBeliefs += belief }

    override fun addGoal(goal: Goal) { initialGoals += goal }

    override fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Env, *, *>) { beliefPlans += plan }

    override fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Env, *, *>) { goalPlans += plan }

    override fun build(): Agent<Belief, Goal, Env> = AgentImpl(initialBeliefs, initialGoals, beliefPlans, goalPlans)

}

private class BeliefBuilderImpl<Belief : Any>(val addBelief: (Belief) -> Unit) : BeliefBuilder<Belief> {
    override operator fun Belief.unaryPlus() { addBelief(this) }
}

private class GoalBuilderImpl<Goal : Any>(val addGoal: (Goal) -> Unit) : GoalBuilder<Goal> {
    override operator fun Goal.not() { addGoal(this) }
}

private class PlanLibraryBuilderImpl<Belief: Any, Goal: Any, Env: Environment>(
    val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Env, *, *>) -> Unit,
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit,
) : PlanLibraryBuilder<Belief, Goal, Env> {
    override val adding: TriggerBuilder.Addition<Belief, Goal, Env>
        get() = TriggerAdditionImpl(addBeliefPlan, addGoalPlan)
    override val removing: TriggerBuilder.Removal<Belief, Goal, Env>
        get() = TriggerRemovalImpl(addBeliefPlan, addGoalPlan)
    override val failing: TriggerBuilder.FailureInterception<Belief, Goal, Env>
        get() = TriggerFailureInterceptionImpl(addGoalPlan)

    override fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Env, *, *>) { addBeliefPlan(plan) }

    override fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Env, *, *>) { addGoalPlan(plan) }
}

private class TriggerAdditionImpl<Belief: Any, Goal: Any, Env: Environment>(
    val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Env, *, *>) -> Unit,
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit
) : TriggerBuilder.Addition<Belief, Goal, Env> {
    override fun <Context : Any> belief(beliefQuery: Belief.() -> Context?): PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> =
        BeliefAdditionPlanBuilderImpl(addBeliefPlan, beliefQuery)

    override fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.Addition.Goal<Belief, Goal, Env, Context> =
        GoalAdditionPlanBuilderImpl(addGoalPlan, goalQuery)
}

private class TriggerRemovalImpl<Belief: Any, Goal: Any, Env: Environment>(
    val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Env, *, *>) -> Unit,
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit
) : TriggerBuilder.Removal<Belief, Goal, Env> {
    override fun <Context : Any> belief(beliefQuery: Belief.() -> Context?): PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> =
        BeliefRemovalPlanBuilderImpl(addBeliefPlan, beliefQuery)

    override fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.Removal.Goal<Belief, Goal, Env, Context> =
        GoalRemovalPlanBuilderImpl(addGoalPlan, goalQuery)
}

private class TriggerFailureInterceptionImpl<Belief: Any, Goal: Any, Env: Environment>(
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit
) : TriggerBuilder.FailureInterception<Belief, Goal, Env> {
    override fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context> =
        GoalFailurePlanBuilderImpl(addGoalPlan, goalQuery)
}


private class BeliefAdditionPlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Env, *, *>) -> Unit,
    val trigger: Belief.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
    : PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> = this.also{this.guard = guard}

    override fun <PlanResult> triggers(
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
        resultType: KType
    ) : Plan.Belief.Addition<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::BeliefAdditionPlan, addBeliefPlan)
}


private class GoalAdditionPlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit,
    val trigger: Goal.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.Addition.Goal<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
            : PlanBuilder.Addition.Goal<Belief, Goal, Env, Context> = this.also{this.guard = guard}

    override fun <PlanResult> triggers(
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
        resultType: KType
    ) : Plan.Goal.Addition<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::GoalAdditionPlan, addGoalPlan)
}

private class BeliefRemovalPlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Env, *, *>) -> Unit,
    val trigger: Belief.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
            : PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> = this.also{this.guard = guard}

    override fun <PlanResult> triggers(
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
        resultType: KType
    ) : Plan.Belief.Removal<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::BeliefRemovalPlan, addBeliefPlan)

}

private class GoalRemovalPlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit,
    val trigger: Goal.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.Removal.Goal<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
            : PlanBuilder.Removal.Goal<Belief, Goal, Env, Context> = this.also{this.guard = guard}

    override fun <PlanResult> triggers(
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
        resultType: KType
    ) : Plan.Goal.Removal<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::GoalRemovalPlan, addGoalPlan)
}

private class GoalFailurePlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit,
    val trigger: Goal.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
            : PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context> = this.also{this.guard = guard}

    override fun <PlanResult> triggers(
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
        resultType: KType
    ) : Plan.Goal.Failure<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::GoalFailurePlan, addGoalPlan)
}


private fun <B : Any, G : Any, E : Environment, TE : Any, C : Any, PR, P : Plan<B, G, E, TE, C, PR>> buildAndRegisterPlan(
    resultType: KType,
    trigger: TE.() -> C?,
    guard: GuardScope<B>.(C) -> C?,
    body: suspend PlanScope<B, G, E, C>.() -> PR,
    builder: ((TE) -> C?, GuardScope<B>.(C) -> C?, suspend PlanScope<B, G, E, C>.() -> PR, KType) -> P,
    register: (P) -> Unit
): P = builder(trigger, guard, body, resultType).also { register(it) }

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

