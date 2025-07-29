

package agent

import kotlinx.coroutines.CompletableDeferred
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface Event {
    @OptIn(ExperimentalUuidApi::class)
    val id: String
        get() = Uuid.random().toString()
}

data class AchieveEvent<T>(
    val planTrigger : String,
    val completion : CompletableDeferred<T> = CompletableDeferred(),
    val args: List<Any?> = emptyList(),
    val intention: Intention? = null
) : Event


data class BeliefAddEvent<T : Any>(
    val beliefName: String,
    val value: T
) : Event

object StepEvent : Event {}


//TODO other types of events