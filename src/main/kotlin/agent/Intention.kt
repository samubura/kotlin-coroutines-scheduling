package agent

import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Intention(
    @OptIn(ExperimentalUuidApi::class)
    val id : String = Uuid.random().toString()
)

class IntentionContext(val intention: Intention, val job: Job) : CoroutineContext.Element{
    override val key: CoroutineContext.Key<IntentionContext> = Key
    companion object Key : CoroutineContext.Key<IntentionContext>
}
