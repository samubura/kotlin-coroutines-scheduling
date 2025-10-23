package api.intention

import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.CoroutineContext.Key

sealed interface Intention : CoroutineContext.Element {
    val id : IntentionID
    val continuation: () -> Unit

    /**
     * Parent of all the chain of sub-executions of continuations.
     * If one child exits, the failure can be detected by the parent.
     * If parent dies, all children die too.
     */
    val job : Job

    companion object Key : CoroutineContext.Key<Intention> {
        operator fun invoke(
            id: IntentionID = IntentionID(),
            continuation: () -> Unit = {},
            job : Job,
        ): Intention = IntentionImpl(id, continuation, job)
    }
}

internal class IntentionImpl(
    override val id: IntentionID = IntentionID(),
    override val continuation: () -> Unit = {},
    override val job: Job,
): Intention {
    override val key: CoroutineContext.Key<Intention> = Intention.Key

    override fun equals(other: Any?): Boolean {
        return (this === other && id == other.id)
    }

    override fun hashCode(): Int = id.hashCode()
}
