package api.intention

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.CoroutineContext.Key

sealed interface Intention : CoroutineContext.Element {
    val id : IntentionID

    /**
     * Parent of all the chain of sub-executions of continuations.
     * If one child exits, the failure can be detected by the parent.
     * If parent dies, all children die too.
     */
    val job : Job

    /**
     * The channel for executing intentions, allows for round-robin fair execution of intentions.
     * New intentions added in the channel are executed on a first-come-first-served basis,
     * however it does NOT allow for setting up priorities.
     */
    val continuations: Channel<() -> Unit>

    /**
     * Executes one step of the intention, i.e. executes its body until the next suspension.
     */
    fun step(): Unit

    /**
     * The intention is resumed after a suspension.
     */
    fun <T> resumeWith(continuation: Continuation<T>, result: Result<T>): Unit

    companion object Key : CoroutineContext.Key<Intention> {
        operator fun invoke(
            id: IntentionID = IntentionID(),
            job : Job,
            continuations: Channel<() -> Unit> = Channel(Channel.UNLIMITED),
        ): Intention = IntentionImpl(id, job, continuations)
    }
}

internal class IntentionImpl(
    override val id: IntentionID = IntentionID(),
    override val job: Job,
    override val continuations: Channel<() -> Unit> = Channel(Channel.UNLIMITED),
): Intention {
    override val key: CoroutineContext.Key<Intention> = Intention.Key

    override fun equals(other: Any?): Boolean {
        return (other is Intention && id == other.id)
    }

    override fun hashCode(): Int = id.hashCode()

    override fun step() {
        continuations.tryReceive().getOrNull()?.let {
            println("Execution of one step of the intention.")
            it()
        }
    }

    override fun <T> resumeWith(continuation: Continuation<T>, result: Result<T>) {
        continuations.trySend {
            continuation.resumeWith(result)
        }
    }
}
