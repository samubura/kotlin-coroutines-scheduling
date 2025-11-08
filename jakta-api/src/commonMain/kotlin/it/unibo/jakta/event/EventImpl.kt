package it.unibo.jakta.event

import it.unibo.jakta.intention.Intention
import kotlinx.coroutines.CompletableDeferred
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class GoalAddEvent<G : Any, PlanResult>(
    override val goal: G,
    override val resultType: KType,
    override val completion: CompletableDeferred<PlanResult>? = null,
    override val intention: it.unibo.jakta.intention.Intention? = null,
) : it.unibo.jakta.event.Event.Internal.Goal.Add<G, PlanResult> {
    companion object {
        fun <G : Any> withNoResult(goal: G) =
            _root_ide_package_.it.unibo.jakta.event.GoalAddEvent<G, Unit>(goal, typeOf<Any>())
    }
}

data class GoalRemoveEvent<G : Any, PlanResult>(
    override val goal: G,
    override val completion: CompletableDeferred<PlanResult>? = null,
    override val intention: it.unibo.jakta.intention.Intention? = null,
    override val resultType: KType,
) : it.unibo.jakta.event.Event.Internal.Goal.Remove<G, PlanResult>

data class GoalFailedEvent<G : Any, PlanResult>(
    override val goal: G,
    override val completion: CompletableDeferred<PlanResult>? = null,
    override val intention: it.unibo.jakta.intention.Intention? = null,
    override val resultType: KType,
) : it.unibo.jakta.event.Event.Internal.Goal.Failed<G, PlanResult>

data class BeliefAddEvent<B : Any>(override val belief: B, override val intention: it.unibo.jakta.intention.Intention? = null) :
    it.unibo.jakta.event.Event.Internal.Belief.Add<B>

data class BeliefRemoveEvent<B : Any>(override val belief: B, override val intention: it.unibo.jakta.intention.Intention? = null) :
    it.unibo.jakta.event.Event.Internal.Belief.Remove<B>
