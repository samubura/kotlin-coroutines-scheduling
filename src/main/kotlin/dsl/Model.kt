package dsl

import kotlin.reflect.KType

interface MAS<Belief : Any, Goal : Any, Env : Environment >{
    val environment: Env
    val agents : Set<Agent<Belief, Goal, Env>>

    fun run() : Unit
}

interface Environment

interface Agent<Belief : Any, Goal: Any,  Env : Environment> {
    val beliefs: Collection<Belief>
    val beliefPlans: List<Plan.Belief<Belief, Goal, Env, *, *>>
    val goalPlans : List<Plan.Goal<Belief, Goal, Env, *, *>>
    suspend fun <PlanResult> achieve(goal: Goal) : PlanResult
}

sealed interface Plan<Belief : Any, Goal: Any,  Env : Environment, TriggerEntity : Any, Context: Any, PlanResult> {
    val trigger: (TriggerEntity) -> Context?
    val guard : GuardScope<Belief>.(Context) -> Context?
    val body :  suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult

    //TODO val resultType : KType

    fun isRelevant(e: TriggerEntity, desiredResult: KType) : Boolean =
        //TODO resultType == desiredResult &&
        this.trigger(e) != null

    fun isApplicable(guardScope: GuardScope<Belief>, e : TriggerEntity, desiredResult: KType) : Boolean =
        //TODO resultType == desiredResult &&
            when (val trig = trigger(e)) {
                null -> false
                else -> when (val g = guard) {
                    else -> g(guardScope, trig) != null
                }
            }

    fun getPlanContext(guardScope: GuardScope<Belief>, e: TriggerEntity) : Context? =
        when (val trig = trigger(e)) {
            null -> null
            else -> when (val g = guard) {
                else -> g(guardScope, trig)
            }
        }

    sealed interface Belief<Belief : Any, Goal: Any,  Env : Environment, Context: Any, PlanResult>
        : Plan<Belief, Goal, Env, Belief,  Context, PlanResult> {

        interface Addition<Belief : Any, Goal: Any,  Env : Environment, Context: Any, PlanResult>
        : Plan.Belief<Belief, Goal, Env, Context, PlanResult>

        interface Removal<Belief : Any, Goal: Any,  Env : Environment, Context: Any, PlanResult>
            : Plan.Belief<Belief, Goal, Env, Context, PlanResult>
    }

    sealed interface Goal<Belief : Any, Goal: Any,  Env : Environment, Context: Any, PlanResult>
        : Plan<Belief, Goal, Env, Goal,  Context, PlanResult> {

        interface Addition<Belief : Any, Goal: Any,  Env : Environment, Context: Any, PlanResult>
            : Plan.Goal<Belief, Goal, Env, Context, PlanResult>

        interface Removal<Belief : Any, Goal: Any,  Env : Environment, Context: Any, PlanResult>
            : Plan.Goal<Belief, Goal, Env, Context, PlanResult>

        interface Failure<Belief : Any, Goal: Any,  Env : Environment, Context: Any, PlanResult>
            : Plan.Goal<Belief, Goal, Env, Context, PlanResult>
    }
}

@JaktaDSL
interface PlanScope<Belief : Any, Goal: Any, Env : Environment, Context : Any> {
    val agent: Agent<Belief, Goal, Env> //TODO probably a different interface with only the "legal" side effects
    val environment: Env
    val context : Context
}

@JaktaDSL
interface GuardScope<Belief : Any> {
    val beliefs : Collection<Belief>
}

//////////////////////////////////////////////////////////////////////
// DEFAULT IMPLEMENTATION
//////////////////////////////////////////////////////////////////////


@JaktaDSL
data class PlanScopeImpl<Belief : Any, Goal : Any, Env : Environment, Context : Any>(
    override val agent: Agent<Belief, Goal, Env>,
    override val environment: Env,
    override val context: Context
) : PlanScope<Belief, Goal, Env, Context>

@JaktaDSL
data class GuardScopeImpl<Belief : Any>(
    override val beliefs: Collection<Belief>
) : GuardScope<Belief>


data class MASImpl<Belief : Any, Goal : Any, Env : Environment>(
    override val environment: Env,
    override val agents: Set<Agent<Belief, Goal, Env>>
) : MAS<Belief, Goal, Env> {

    override fun run() {
        TODO()
    }
}

data class AgentImpl<Belief : Any, Goal : Any, Env : Environment>(
    override val beliefs: Collection<Belief>,
    override val beliefPlans: List<Plan.Belief<Belief, Goal, Env, *, *>>,
    override val goalPlans: List<Plan.Goal<Belief, Goal, Env, *, *>>
) : Agent<Belief, Goal, Env> {

    override suspend fun <PlanResult> achieve(goal: Goal): PlanResult {
        return TODO()
    }
}

data class BeliefAdditionPlan<Belief: Any, Goal: Any, Env: Environment, Context: Any, PlanResult>(
    override val trigger: (Belief) -> Context?,
    override val guard: GuardScope<Belief>.(Context) -> Context?,
    override val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult,
    // override val resultType: KType

): Plan.Belief.Addition<Belief, Goal, Env, Context, PlanResult>

data class BeliefRemovalPlan<Belief: Any, Goal: Any, Env: Environment, Context: Any, PlanResult>(
    override val trigger: (Belief) -> Context?,
    override val guard: GuardScope<Belief>.(Context) -> Context?,
    override val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult,
    // override val resultType: KType

): Plan.Belief.Removal<Belief, Goal, Env, Context, PlanResult>


data class GoalAdditionPlan<Belief: Any, Goal: Any, Env: Environment, Context: Any, PlanResult>(
    override val trigger: (Goal) -> Context?,
    override val guard: GuardScope<Belief>.(Context) -> Context?,
    override val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult,
    // override val resultType: KType

): Plan.Goal.Addition<Belief, Goal, Env, Context, PlanResult>

data class GoalRemovalPlan<Belief: Any, Goal: Any, Env: Environment, Context: Any, PlanResult>(
    override val trigger: (Goal) -> Context?,
    override val guard: GuardScope<Belief>.(Context) -> Context?,
    override val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult,
    // override val resultType: KType

): Plan.Goal.Removal<Belief, Goal, Env, Context, PlanResult>

data class GoalFailurePlan<Belief: Any, Goal: Any, Env: Environment, Context: Any, PlanResult>(
    override val trigger: (Goal) -> Context?,
    override val guard: GuardScope<Belief>.(Context) -> Context?,
    override val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult,
    // override val resultType: KType

): Plan.Goal.Failure<Belief, Goal, Env, Context, PlanResult>





