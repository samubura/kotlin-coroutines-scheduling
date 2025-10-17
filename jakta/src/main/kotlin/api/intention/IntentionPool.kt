package api.intention

import api.event.Event
import api.plan.Plan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.job

interface IntentionPool {
    /**
     * Given an event, it returns the intention to execute it.
     */
    suspend fun CoroutineScope.nextIntention(event: Event.Internal): Intention

}

interface AddableIntentionPool : IntentionPool {
    fun tryPut(intention: Intention) : Boolean
}

interface MutableIntentionPool : AddableIntentionPool {
    suspend fun drop(intentionID: IntentionID) : Boolean
}

class MutableIntentionPoolImpl: MutableIntentionPool {

    /** List of intentions currently managed by the agent. **/
    private val intentions: MutableSet<Intention> = mutableSetOf()

    /**
     * The channel for executing intentions, allows for round-robin fair execution of intentions.
     * New intentions added in the channel are executed on a first-come-first-served basis,
     * however it does NOT allow for setting up priorities.
     */
    private val continuations: Channel<() -> Unit> = Channel(Channel.UNLIMITED)

    override suspend fun drop(intentionID: IntentionID): Boolean =
        intentions.find { it.id == intentionID }?.let {
            it.job.cancel() // Cancel the job associated to the intention
            intentions.remove(it)
        } ?: false

    override fun tryPut(intention: Intention): Boolean = intentions.add(intention)

    override suspend fun CoroutineScope.nextIntention(event: Event.Internal): Intention {
        val nextIntention = event.intention?.let {
            // If the referenced intention exists, use its context
            intentions.find { intention -> intention == event.intention } ?: run {
                // If the referenced intention does not exist, create a new one with that ID
                // This is useful for debugging purposes, as it allows to name intentions
                Intention(it.id, it.continuation, Job(coroutineContext.job), )
            }
        } ?: run {
            Intention(job = Job(coroutineContext.job)) // TODO(Double-check, i'm not sure)
        }

        tryPut(nextIntention)
        return nextIntention
    }

}

