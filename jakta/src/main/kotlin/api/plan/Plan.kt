package api.plan

import api.agent.AgentActions
import api.environment.Environment
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

sealed interface Plan<Belief : Any, Goal : Any, Env : Environment, TriggerEntity : Any, Context : Any, PlanResult> {
    val id: PlanID
        get() = PlanID() // TODO check
    val trigger: (TriggerEntity) -> Context?
    val guard: GuardScope<Belief>.(Context) -> Context?
    val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult
    val resultType: KType

    fun isRelevant(e: TriggerEntity, desiredResult: KType = typeOf<Any>()): Boolean =
        resultType.isSubtypeOf(desiredResult) &&
            this.trigger(e) != null

    fun isApplicable(guardScope: GuardScope<Belief>, e: TriggerEntity, desiredResult: KType = typeOf<Any>()): Boolean =
        resultType.isSubtypeOf(desiredResult) &&
            trigger(e)?.let { guard(guardScope, it) != null } ?: false

    /**
     * @return the Context of this Plan applied to the trigger and the guard.
     * The invocation of this method is supposed to be performed during execution,
     * this implies that this plan has a valid context that can be executed.
     * If this is not the case, an [IllegalStateException] is thrown.
     */
    private fun getPlanContext(guardScope: GuardScope<Belief>, e: TriggerEntity): Context = trigger(e)?.also {
        guard(guardScope, it)
    } ?: throw IllegalStateException("Execution not possible without a plan context")

    suspend fun run(
        agent: AgentActions<Belief, Goal>,
        guardScope: GuardScope<Belief>,
        environment: Env,
        entity: TriggerEntity,
    ): PlanResult = body(PlanScopeImpl(agent, environment, getPlanContext(guardScope, entity)))

    // TODO It does not make sense for Belief Plans to have a PlanResult as it will never be awaited on... right?
    // What are the implication on the overall design? Remove it or bind it as Unit?
    sealed interface Belief<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
        Plan<B, G, Env, B, Context, PlanResult> {
        interface Addition<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
            Belief<B, G, Env, Context, PlanResult>

        interface Removal<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
            Belief<B, G, Env, Context, PlanResult>
    }

    sealed interface Goal<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
        Plan<B, G, Env, G, Context, PlanResult> {
        interface Addition<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
            Goal<B, G, Env, Context, PlanResult>

        interface Removal<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
            Goal<B, G, Env, Context, PlanResult>

        interface Failure<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
            Goal<B, G, Env, Context, PlanResult>
    }
}
