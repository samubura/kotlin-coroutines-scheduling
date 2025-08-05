

package agent

import kotlinx.coroutines.CompletableDeferred
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface Event {
    @OptIn(ExperimentalUuidApi::class)
    val id: String
        get() = Uuid.random().toString()
}


//TODO use the other types of events

data class AchieveEvent<T>(
    val planTrigger : String,
    val completion : CompletableDeferred<T> = CompletableDeferred(),
    val args: List<Any?> = emptyList(),
    val intention: Intention? = null
) : Event

data class AchieveFailedEvent(
    val planTrigger: String,
    val args: List<Any?> = emptyList(),
    val intention: Intention? = null
) : Event

data class BeliefAddEvent<T : Any?>(
    val beliefName: String,
    val value: T
) : Event

data class BeliefRemoveEvent(
    val beliefName: String
) : Event

data class TestEvent(
    val testTrigger: String
) : Event

object StepEvent : Event {}

//TODO what about EXTERNAL events??
// e.g., PerceptionEvent, ArtifactEvent, KQMLMessageEvent....
// It would be cool to have a way to use a plugin-based system to add custom event-processing pipelines
// so that an agent can be extended with the ability to handle custom events and convert them into internal events
// or maybe even handling them directly with specialized plans...

//TODO are plans already a plugin-based system? You can plug sets of plans into an agent.
// Jason does it like that, e.g. for KQML using "atomic" plans...
// (which we don't need probably since we have coroutines, as long as the plan never suspends)
// But how would we write those plans? Are they "externalEvent" plans, matching in sequence
// i.e. like handlers with a "next()" function to pass over the event to the next available plan?

