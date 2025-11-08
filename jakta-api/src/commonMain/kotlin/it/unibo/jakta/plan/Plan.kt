package it.unibo.jakta.plan

import it.unibo.jakta.agent.AgentActions
import it.unibo.jakta.environment.Environment
import kotlin.reflect.KType
import kotlin.reflect.typeOf

//TODO can we avoid needing this?
import it.unibo.jakta.reflection.isSubtypeOfMultiPlatform

sealed interface Plan<Belief : Any, Goal : Any, Env : it.unibo.jakta.environment.Environment, TriggerEntity : Any, Context : Any, PlanResult> {
    val id: it.unibo.jakta.plan.PlanID
        get() = _root_ide_package_.it.unibo.jakta.plan.PlanID() // TODO check
    val trigger: (TriggerEntity) -> Context?
    val guard: it.unibo.jakta.plan.GuardScope<Belief>.(Context) -> Context?
    val body: suspend (it.unibo.jakta.plan.PlanScope<Belief, Goal, Env, Context>) -> PlanResult
    val resultType: KType

    fun isRelevant(e: TriggerEntity, desiredResult: KType = typeOf<Any>()): Boolean =
        resultType.isSubtypeOfMultiPlatform(desiredResult) &&
            this.trigger(e) != null

    fun isApplicable(guardScope: it.unibo.jakta.plan.GuardScope<Belief>, e: TriggerEntity, desiredResult: KType = typeOf<Any>()): Boolean =
        resultType.isSubtypeOfMultiPlatform(desiredResult) &&
            trigger(e)?.let { guard(guardScope, it) != null } ?: false

    /**
     * @return the Context of this Plan applied to the trigger and the guard.
     * The invocation of this method is supposed to be performed during execution,
     * this implies that this plan has a valid context that can be executed.
     * If this is not the case, an [IllegalStateException] is thrown.
     */
    private fun getPlanContext(guardScope: it.unibo.jakta.plan.GuardScope<Belief>, e: TriggerEntity): Context = trigger(e)?.also {
        guard(guardScope, it)
    } ?: throw IllegalStateException("Execution not possible without a plan context")

    suspend fun run(
        agent: it.unibo.jakta.agent.AgentActions<Belief, Goal>,
        guardScope: it.unibo.jakta.plan.GuardScope<Belief>,
        environment: Env,
        entity: TriggerEntity,
    ): PlanResult = body(
        _root_ide_package_.it.unibo.jakta.plan.PlanScopeImpl(
            agent,
            environment,
            getPlanContext(guardScope, entity)
        )
    )

    // TODO It does not make sense for Belief Plans to have a PlanResult as it will never be awaited on... right?
    // What are the implication on the overall design? Remove it or bind it as Unit?
    sealed interface Belief<B : Any, G : Any, Env : it.unibo.jakta.environment.Environment, Context : Any, PlanResult> :
        it.unibo.jakta.plan.Plan<B, G, Env, B, Context, PlanResult> {
        interface Addition<B : Any, G : Any, Env : it.unibo.jakta.environment.Environment, Context : Any, PlanResult> :
            Belief<B, G, Env, Context, PlanResult>

        interface Removal<B : Any, G : Any, Env : it.unibo.jakta.environment.Environment, Context : Any, PlanResult> :
            Belief<B, G, Env, Context, PlanResult>
    }

    sealed interface Goal<B : Any, G : Any, Env : it.unibo.jakta.environment.Environment, Context : Any, PlanResult> :
        it.unibo.jakta.plan.Plan<B, G, Env, G, Context, PlanResult> {
        interface Addition<B : Any, G : Any, Env : it.unibo.jakta.environment.Environment, Context : Any, PlanResult> :
            Goal<B, G, Env, Context, PlanResult>

        interface Removal<B : Any, G : Any, Env : it.unibo.jakta.environment.Environment, Context : Any, PlanResult> :
            Goal<B, G, Env, Context, PlanResult>

        interface Failure<B : Any, G : Any, Env : it.unibo.jakta.environment.Environment, Context : Any, PlanResult> :
            Goal<B, G, Env, Context, PlanResult>
    }
}
