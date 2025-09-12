package api.plan

import api.environment.Environment
import kotlin.reflect.KType


data class BeliefAdditionPlan<Belief: Any, Goal: Any, Env: Environment, Context: Any, PlanResult>(
    override val trigger: (Belief) -> Context?,
    override val guard: GuardScope<Belief>.(Context) -> Context?,
    override val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult,
    override val resultType: KType

): Plan.Belief.Addition<Belief, Goal, Env, Context, PlanResult>

data class BeliefRemovalPlan<Belief: Any, Goal: Any, Env: Environment, Context: Any, PlanResult>(
    override val trigger: (Belief) -> Context?,
    override val guard: GuardScope<Belief>.(Context) -> Context?,
    override val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult,
    override val resultType: KType

): Plan.Belief.Removal<Belief, Goal, Env, Context, PlanResult>


data class GoalAdditionPlan<Belief: Any, Goal: Any, Env: Environment, Context: Any, PlanResult>(
    override val trigger: (Goal) -> Context?,
    override val guard: GuardScope<Belief>.(Context) -> Context?,
    override val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult,
    override val resultType: KType

): Plan.Goal.Addition<Belief, Goal, Env, Context, PlanResult>

data class GoalRemovalPlan<Belief: Any, Goal: Any, Env: Environment, Context: Any, PlanResult>(
    override val trigger: (Goal) -> Context?,
    override val guard: GuardScope<Belief>.(Context) -> Context?,
    override val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult,
    override val resultType: KType

): Plan.Goal.Removal<Belief, Goal, Env, Context, PlanResult>

data class GoalFailurePlan<Belief: Any, Goal: Any, Env: Environment, Context: Any, PlanResult>(
    override val trigger: (Goal) -> Context?,
    override val guard: GuardScope<Belief>.(Context) -> Context?,
    override val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult,
    override val resultType: KType
): Plan.Goal.Failure<Belief, Goal, Env, Context, PlanResult>