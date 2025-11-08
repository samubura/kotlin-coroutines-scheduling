package it.unibo.jakta.plan

import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.environment.Environment


@JaktaDSL
interface PlanLibraryBuilder<Belief : Any, Goal : Any, Env : Environment> {
    val adding: TriggerBuilder.Addition<Belief, Goal, Env>
    val removing: TriggerBuilder.Removal<Belief, Goal, Env>
    val failing: TriggerBuilder.FailureInterception<Belief, Goal, Env>

    fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Env, *, *>)

    fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Env, *, *>)
}

class PlanLibraryBuilderImpl<Belief : Any, Goal : Any, Env : Environment>(
    val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Env, *, *>) -> Unit,
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit,
) : PlanLibraryBuilder<Belief, Goal, Env> {
    override val adding: TriggerBuilder.Addition<Belief, Goal, Env>
        get() = TriggerAdditionImpl(addBeliefPlan, addGoalPlan)
    override val removing: TriggerBuilder.Removal<Belief, Goal, Env>
        get() = TriggerRemovalImpl(addBeliefPlan, addGoalPlan)
    override val failing: TriggerBuilder.FailureInterception<Belief, Goal, Env>
        get() = TriggerFailureInterceptionImpl(addGoalPlan)

    override fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Env, *, *>) {
        addBeliefPlan(plan)
    }

    override fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Env, *, *>) {
        addGoalPlan(plan)
    }
}
