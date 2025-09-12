package api.event

import api.intention.Intention
import kotlinx.coroutines.CompletableDeferred

data class GoalAddEvent <G: Any, PlanResult>(
    override val goal: G,
    override val completion: CompletableDeferred<PlanResult>? = null,
    override val intention: Intention? = null,
) : Event.Internal.Goal.Add<G, PlanResult>

data class GoalRemoveEvent <G: Any, PlanResult>(
    override val goal: G,
    override val completion: CompletableDeferred<PlanResult>? = null,
    override val intention: Intention? = null,
) : Event.Internal.Goal.Remove<G, PlanResult>

data class GoalFailedEvent <G: Any, PlanResult>(
    override val goal: G,
    override val completion: CompletableDeferred<PlanResult>? = null,
    override val intention: Intention? = null,
) : Event.Internal.Goal.Failed<G, PlanResult>

data class BeliefAddEvent <B: Any>(
    override val belief: B,
    override val intention: Intention? = null
) : Event.Internal.Belief.Add<B>

data class BeliefRemoveEvent <B: Any>(
    override val belief: B,
    override val intention: Intention? = null
) : Event.Internal.Belief.Remove<B>


