package api.event

import api.intention.Intention
import kotlinx.coroutines.CompletableDeferred
import kotlin.reflect.KType

sealed interface Event {
    sealed interface Internal : Event {
        val intention: Intention? //TODO needed?

        sealed interface Goal<G :Any, PlanResult> : Internal {
            val completion: CompletableDeferred<PlanResult>?
            val goal: G
            val resultType : KType

            interface Add<G : Any, PlanResult> : Goal<G, PlanResult>

            interface Remove<G : Any, PlanResult> : Goal <G, PlanResult>

            interface Failed<G: Any, PlanResult> : Goal <G, PlanResult>
        }

        sealed interface Belief<B : Any> : Internal {
            val belief: B

            interface Add<B : Any> : Belief<B>

            interface Remove<B : Any> : Belief<B>
        }

        data class Step(override val intention: Intention) : Internal
    }

    //TODO external events
    //interface External : Event
}