@file:OptIn(ExperimentalUuidApi::class)

package agent

import kotlinx.coroutines.CompletableDeferred
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface Event {
    val id : String
        get() = Uuid.random().toString()
}

data class AchieveEvent<T>(
    val planTrigger : String,
    val completion : CompletableDeferred<T> = CompletableDeferred(),
    val args: Sequence<Any?> = emptySequence()
) : Event


data class BeliefAddEvent<T : Any>(
    val beliefName: String,
    val value: T
) : Event

object StepEvent : Event {}


//TODO other types of events