@file:OptIn(ExperimentalUuidApi::class)

package agent

import kotlinx.coroutines.CompletableDeferred
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface Event{
    val id : String
        get() = Uuid.random().toString()
}

//TODO handle typed deferred
data class AchieveEvent(
    val planTrigger : String,
    val completion : CompletableDeferred<Unit> = CompletableDeferred(),
    val intentionID : String? = null
) : Event


//TODO other types of events