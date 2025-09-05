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
    //TODO FIX GENERICS:
    // the first is (Goal | Belief) but the other ones?
    // I think it is ok if they are Any as we won't have restrictions and each plan can have:
    // - custom trigger result (?)
    // - custom guard result i.e. context, which can also be simply TriggerResult when there is no guard so basically Context = (TriggerResult | Context)
    // - custom plan result (which we store as a KType to find plans with reflection)

    val plans: List<Plan<Belief, Goal, Env, Any, Any, Any, Any>>
    suspend fun <PlanResult> achieve(goal: Goal) : PlanResult
}

//TODO THIS IS THE SOURCE OF ALL ISSUES
// Should we build and keep two parallel collections of plans? One for beliefs and one for goals?
// Note that this will only solve the issue of the "TriggerEntity" generic
// Which I have already fixed with a split PlanBuilder
sealed interface Plan<Belief : Any, Goal: Any,  Env : Environment, TriggerEntity : Any, TriggerResult : Any, Context: Any, PlanResult> {
    val trigger: (TriggerEntity) -> TriggerResult?
    val guard : ((Collection<Belief>, TriggerResult) -> Context?)?
    val body :  suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult
    val resultType : KType

    fun isRelevant(e: TriggerEntity, desiredResult: KType) : Boolean = resultType == desiredResult && this.trigger(e) != null

    fun isApplicable(beliefs: Collection<Belief>, e : TriggerEntity, desiredResult: KType) : Boolean = resultType == desiredResult &&
            when (val trig = trigger(e)) {
                null -> false
                else -> when (val g = guard) {
                    null -> true
                    else -> g(beliefs, trig) != null
                }
            }

    fun getPlanContext(beliefs: Collection<Belief>, e: TriggerEntity) : Context? =
        when (val trig = trigger(e)) {
            null -> null
            else -> when (val g = guard) {
                null -> trig as Context? //TODO check ma mi sembra giusto questo cast non dovrebbe mai saltare
                else -> g(beliefs, trig)
            }
        }

    interface Belief<Belief : Any, Goal: Any,  Env : Environment, TriggerResult : Any, Context: Any, PlanResult>
        : Plan<Belief, Goal, Env, Belief, TriggerResult, Context, PlanResult>

    interface Goal<Belief : Any, Goal: Any,  Env : Environment, TriggerResult : Any, Context: Any, PlanResult>
        : Plan<Belief, Goal, Env, Goal, TriggerResult, Context, PlanResult>
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
    override val plans: List<Plan<Belief, Goal, Env, Any, Any, Any, Any>>
) : Agent<Belief, Goal, Env> {

    override suspend fun <PlanResult> achieve(goal: Goal): PlanResult {
        return TODO()
    }
}

data class BeliefPlanImpl<Belief : Any, Goal: Any,  Env : Environment, TriggerResult : Any, Context: Any, PlanResult>(
    override val trigger: (Belief) -> TriggerResult?,
    override val guard: ((Collection<Belief>, TriggerResult) -> Context?)?,
    override val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult,
    override val resultType: KType
) : Plan.Belief<Belief, Goal, Env, TriggerResult, Context, PlanResult>

data class GoalPlanImpl<Belief : Any, Goal: Any,  Env : Environment, TriggerResult : Any, Context: Any, PlanResult>(
    override val trigger: (Goal) -> TriggerResult?,
    override val guard: ((Collection<Belief>, TriggerResult) -> Context?)?,
    override val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult,
    override val resultType: KType
) : Plan.Goal<Belief, Goal, Env, TriggerResult, Context, PlanResult>

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



