package dsl.plan

import api.environment.Environment
import api.plan.Plan
import dsl.JaktaDSL


@JaktaDSL
sealed interface TriggerBuilder<Belief : Any, Goal : Any, Env : Environment> {

    sealed interface Addition<Belief : Any, Goal : Any, Env : Environment> :
        TriggerBuilder<Belief, Goal, Env>{

        fun <Context : Any> belief(beliefQuery: Belief.() -> Context?)
                : PlanBuilder.Addition.Belief<Belief, Goal, Env, Context>

        fun <Context : Any> goal(goalQuery: Goal.() -> Context?)
                : PlanBuilder.Addition.Goal<Belief, Goal, Env, Context>
    }

    sealed interface Removal<Belief : Any, Goal : Any, Env : Environment> :
        TriggerBuilder<Belief, Goal, Env>{

        fun <Context : Any> belief(beliefQuery: Belief.() -> Context?)
                : PlanBuilder.Removal.Belief<Belief, Goal, Env, Context>

        fun <Context : Any> goal(goalQuery: Goal.() -> Context?)
                : PlanBuilder.Removal.Goal<Belief, Goal, Env, Context>
    }

    sealed interface FailureInterception<Belief : Any, Goal : Any, Env : Environment> :
        TriggerBuilder<Belief, Goal, Env>{
        fun <Context : Any> goal(goalQuery: Goal.() -> Context?)
                : PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context>
    }
}


class TriggerAdditionImpl<Belief: Any, Goal: Any, Env: Environment>(
    val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Env, *, *>) -> Unit,
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit
) : TriggerBuilder.Addition<Belief, Goal, Env> {
    override fun <Context : Any> belief(beliefQuery: Belief.() -> Context?): PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> =
        BeliefAdditionPlanBuilderImpl(addBeliefPlan, beliefQuery)

    override fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.Addition.Goal<Belief, Goal, Env, Context> =
        GoalAdditionPlanBuilderImpl(addGoalPlan, goalQuery)
}

class TriggerRemovalImpl<Belief: Any, Goal: Any, Env: Environment>(
    val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Env, *, *>) -> Unit,
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit
) : TriggerBuilder.Removal<Belief, Goal, Env> {
    override fun <Context : Any> belief(beliefQuery: Belief.() -> Context?): PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> =
        BeliefRemovalPlanBuilderImpl(addBeliefPlan, beliefQuery)

    override fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.Removal.Goal<Belief, Goal, Env, Context> =
        GoalRemovalPlanBuilderImpl(addGoalPlan, goalQuery)
}

class TriggerFailureInterceptionImpl<Belief: Any, Goal: Any, Env: Environment>(
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit
) : TriggerBuilder.FailureInterception<Belief, Goal, Env> {
    override fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context> =
        GoalFailurePlanBuilderImpl(addGoalPlan, goalQuery)
}