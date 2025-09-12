package dsl.plan

import api.environment.Environment
import api.plan.BeliefAdditionPlan
import api.plan.BeliefRemovalPlan
import api.plan.GoalAdditionPlan
import api.plan.GoalFailurePlan
import api.plan.GoalRemovalPlan
import api.plan.GuardScope
import api.plan.Plan
import api.plan.PlanScope
import kotlin.reflect.KType


class BeliefAdditionPlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Env, *, *>) -> Unit,
    val trigger: Belief.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
            : PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> = this.also{this.guard = guard}

    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
    ) : Plan.Belief.Addition<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::BeliefAdditionPlan, addBeliefPlan)
}


class GoalAdditionPlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit,
    val trigger: Goal.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.Addition.Goal<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
            : PlanBuilder.Addition.Goal<Belief, Goal, Env, Context> = this.also{this.guard = guard}

    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
    ) : Plan.Goal.Addition<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::GoalAdditionPlan, addGoalPlan)
}

class BeliefRemovalPlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Env, *, *>) -> Unit,
    val trigger: Belief.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
            : PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> = this.also{this.guard = guard}

    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
    ) : Plan.Belief.Removal<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::BeliefRemovalPlan, addBeliefPlan)

}

class GoalRemovalPlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit,
    val trigger: Goal.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.Removal.Goal<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
            : PlanBuilder.Removal.Goal<Belief, Goal, Env, Context> = this.also{this.guard = guard}

    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
    ) : Plan.Goal.Removal<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::GoalRemovalPlan, addGoalPlan)
}

class GoalFailurePlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any>(
    val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit,
    val trigger: Goal.() -> Context?,
    var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x}
) : PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context> {

    override fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?)
            : PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context> = this.also{this.guard = guard}

    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
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