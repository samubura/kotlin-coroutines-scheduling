package dsl

import kotlin.reflect.KType

//////////////////////////////////////////////////////////////////////
// DSL
//////////////////////////////////////////////////////////////////////

@DslMarker
annotation class JaktaDSL

@JaktaDSL
sealed interface TriggerBuilder<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> {
    sealed interface OnBelief<Belief : Any , Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any>
        : TriggerBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult> {

        fun <PlanResult> belief(beliefQuery: Belief.() -> BeliefQueryResult?)
        : PlanBuilder.Belief<Belief, Goal, Env, BeliefQueryResult, PlanResult>
    }
    sealed interface OnGoal<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any>
        : TriggerBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult> {

        fun <PlanResult> goal(goalQuery: Goal.() -> GoalQueryResult?)
        : PlanBuilder.Goal<Belief, Goal, Env, GoalQueryResult, PlanResult>
    }
}

sealed interface Addition<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> :
    TriggerBuilder.OnBelief<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>,
    TriggerBuilder.OnGoal<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>

sealed interface Removal<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> :
    TriggerBuilder.OnBelief<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>,
    TriggerBuilder.OnGoal<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>

sealed interface FailureInterception<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> :
    TriggerBuilder.OnGoal<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>


@JaktaDSL
interface MasBuilder<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> {

    @JaktaDSL
    fun agent(
        block: AgentBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>.() -> Unit
    ): Agent<Belief, Goal, Env>

    fun environment (block: () -> Env)
}

@JaktaDSL
interface AgentBuilder<Belief : Any, Goal : Any, Env: Environment, BeliefQueryResult : Any, GoalQueryResult : Any> {

    fun believes(
        block: BeliefBuilder<Belief>.() -> Unit
    ) : Collection<Belief>

    fun hasInitialGoals(
        block: GoalBuilder<Goal>.() -> Unit
    ) : Sequence<Goal>

    fun hasPlans(
        block: PlanLibraryBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>.() -> Unit
    ): Sequence<Plan<Belief, Goal, Env, Any, Any, Any, Any>>  // TODO FIX GENERICS
}

@JaktaDSL
interface BeliefBuilder<Belief : Any> {
    operator fun Belief.unaryPlus()
}

@JaktaDSL
interface GoalBuilder<Goal : Any> {
    operator fun Goal.not()
}

@JaktaDSL
interface PlanLibraryBuilder<Belief : Any, Goal : Any, Env: Environment, BeliefQueryResult : Any, GoalQueryResult : Any> {
    val adding: Addition<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>
    val removing: Removal<Belief, Goal, Env,  BeliefQueryResult, GoalQueryResult>
    val failing: FailureInterception<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>
}

//TODO I'm not sure all this extra stuff is needed, it seems too much
@JaktaDSL
sealed interface PlanBuilder<Belief : Any, Goal: Any, Env : Environment, Context : Any, PlanResult> {

    interface Belief<Belief : Any, Goal: Any, Env : Environment, Context : Any, PlanResult> : PlanBuilder<Belief, Goal, Env, Context, PlanResult> {
        infix fun <OutputContext : Any> onlyWhen(guard: GuardScope<Belief>.(Context) -> OutputContext?) :
                GuardedPlanBuilder.Belief<Belief, Goal, Env, Context, OutputContext, PlanResult>

        infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult) :
            Plan.Belief<Belief, Goal, Env, Context, Context, PlanResult>
    }

    interface Goal<Belief : Any, Goal: Any, Env : Environment, Context : Any, PlanResult> : PlanBuilder<Belief, Goal, Env, Context, PlanResult> {
        infix fun <OutputContext : Any> onlyWhen(guard: GuardScope<Belief>.(Context) -> OutputContext?) :
                GuardedPlanBuilder.Goal<Belief, Goal, Env, Context, OutputContext, PlanResult>

        infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult) :
            Plan.Goal<Belief, Goal, Env, Any, Context, PlanResult>
    }
}

// TODO This is definitely duplicating some logic but now I have fully bounded types
sealed interface GuardedPlanBuilder<Belief : Any, Goal: Any, Env : Environment, TriggerContext : Any, OutputContext : Any, PlanResult> {

    interface Belief<Belief : Any, Goal: Any, Env : Environment, TriggerContext : Any, OutputContext : Any, PlanResult> :
        GuardedPlanBuilder<Belief, Goal, Env, TriggerContext, OutputContext, PlanResult> {

        infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, OutputContext>.() -> PlanResult) :
                Plan.Belief<Belief, Goal, Env, TriggerContext, OutputContext, PlanResult>
    }

    interface Goal<Belief : Any, Goal: Any, Env : Environment, TriggerContext : Any, OutputContext : Any, PlanResult> :
        GuardedPlanBuilder<Belief, Goal, Env, TriggerContext, OutputContext, PlanResult> {

        infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, OutputContext>.() -> PlanResult) :
                Plan.Goal<Belief, Goal, Env, TriggerContext, OutputContext, PlanResult>
    }
}