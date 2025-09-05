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

    val plans: List<Plan<Belief, Goal, Env, Any, Any, Any>>
    suspend fun <PlanResult> achieve(goal: Goal) : PlanResult
}

sealed interface Plan<Belief : Any, Goal: Any,  Env : Environment, TriggerEntity : Any, Context: Any, PlanResult> {
    val trigger: (TriggerEntity) -> Context?
    val guard : ((Collection<Belief>, Context) -> Context?)?
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
                null -> trig
                else -> g(beliefs, trig)
            }
        }

    sealed interface Addition<Belief : Any, Goal: Any,  Env : Environment, TriggerEntity: Any, Context: Any, PlanResult>
        : Plan<Belief, Goal, Env, TriggerEntity, Context, PlanResult> {

        interface Belief<Belief : Any, Goal: Any,  Env : Environment, Context: Any, PlanResult>
            : Addition<Belief, Goal, Env, Belief,  Context, PlanResult>

        interface Goal <Belief : Any, Goal: Any,  Env : Environment, Context: Any, PlanResult>
            : Addition<Belief, Goal, Env, Goal,  Context, PlanResult>
    }

    sealed interface Removal<Belief : Any, Goal: Any,  Env : Environment, TriggerEntity: Any, Context: Any, PlanResult>
        : Plan<Belief, Goal, Env, TriggerEntity,  Context, PlanResult> {

        interface Belief<Belief : Any, Goal: Any,  Env : Environment,  Context: Any, PlanResult>
            : Addition<Belief, Goal, Env, Belief,  Context, PlanResult>

        interface Goal <Belief : Any, Goal: Any,  Env : Environment, Context: Any, PlanResult>
            : Addition<Belief, Goal, Env, Goal, Context, PlanResult>
    }

    interface GoalFailure<Belief : Any, Goal: Any,  Env : Environment, Context: Any, PlanResult>
        : Plan<Belief, Goal, Env, Goal, Context, PlanResult>
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
    override val plans: List<Plan<Belief, Goal, Env, Any, Any, Any>>
) : Agent<Belief, Goal, Env> {

    override suspend fun <PlanResult> achieve(goal: Goal): PlanResult {
        return TODO()
    }
}

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



