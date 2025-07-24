@file:OptIn(ExperimentalUuidApi::class)

import kotlinx.coroutines.CompletableDeferred
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface Event{
    val id : String
        get() = Uuid.random().toString()
}

data class AchieveEvent <T>(
    val planTrigger : String,
    val completion : CompletableDeferred<T> = CompletableDeferred(),
    val intentionID : String? = null
) : Event


class StepEvent() : Event