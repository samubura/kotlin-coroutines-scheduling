package api.event

import api.intention.Intention
import kotlinx.coroutines.CompletableDeferred
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class GoalAddEvent<G : Any, PlanResult>(
    override val goal: G,
    override val resultType: KType,
    override val completion: CompletableDeferred<PlanResult>? = null,
    override val intention: Intention? = null,
) : Event.Internal.Goal.Add<G, PlanResult> {
    companion object {
        fun <G : Any> withNoResult(goal: G) = GoalAddEvent<G, Unit>(goal, typeOf<Any>())
    }
}

data class GoalRemoveEvent<G : Any, PlanResult>(
    override val goal: G,
    override val completion: CompletableDeferred<PlanResult>? = null,
    override val intention: Intention? = null,
    override val resultType: KType,
) : Event.Internal.Goal.Remove<G, PlanResult>

data class GoalFailedEvent<G : Any, PlanResult>(
    override val goal: G,
    override val completion: CompletableDeferred<PlanResult>? = null,
    override val intention: Intention? = null,
    override val resultType: KType,
) : Event.Internal.Goal.Failed<G, PlanResult>

data class BeliefAddEvent<B : Any>(override val belief: B, override val intention: Intention? = null) :
    Event.Internal.Belief.Add<B>

data class BeliefRemoveEvent<B : Any>(override val belief: B, override val intention: Intention? = null) :
    Event.Internal.Belief.Remove<B>
