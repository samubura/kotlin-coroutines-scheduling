package api.intention

import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.CoroutineContext.Key

interface Intention : CoroutineContext.Element {
    val id : IntentionID
    val continuation: () -> Unit

    /**
     * Parent of all the chain of sub-executions of continuations.
     * If one child exits, the failure can be detected by the parent.
     * If parent dies, all children die too.
     */
    val job : Job

    companion object {
        operator fun invoke(
            id: IntentionID = IntentionID(),
            continuation: () -> Unit = {},
            job : Job,
        ): Intention = object : Intention {
            override val id: IntentionID = id
            override val job = job
            override val continuation: () -> Unit = continuation

            private val context = object : CoroutineContext.Key<Intention> {}
            override val key: CoroutineContext.Key<Intention> = context
        }
    }
}
