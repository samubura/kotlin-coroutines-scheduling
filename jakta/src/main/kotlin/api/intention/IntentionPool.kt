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
    /**
     * The intention passed as argument is inserted in the intention pool.
     * If the intention was not in the pool, then is added.
     * If the intention was present in the pool, then its content is overridden with the one passed as parameter.
     * If something goes wrong in the process, the method returns false, otherwise true.
     */
    fun tryPut(intention: Intention) : Boolean
}

interface MutableIntentionPool : AddableIntentionPool {
    suspend fun drop(intentionID: IntentionID) : Boolean

    /**
     * Executes one step of the next intention to execute.
     */
    suspend fun stepNextIntention(): Unit
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


    override fun tryPut(intention: Intention): Boolean {
        if (intentions.contains(intention).also { println(intentions) }){
            // I don't use the drop() because I don't want to cancel the job in this invocation.
            intentions.remove(intention)
            println("Updating intention ${intention.id}")
        }
        continuations.trySend { intention.continuation } // TODO(check position of this invocation)
        return intentions.add(intention)
    }

    override suspend fun CoroutineScope.nextIntention(event: Event.Internal): Intention {
        val nextIntention = event.intention?.let {
            // If the referenced intention exists, use its context
            intentions.find { intention -> intention == event.intention } ?: run {
                // If the referenced intention does not exist, create a new one with that ID
                // This is useful for debugging purposes, as it allows to name intentions
                Intention(it.id, it.continuation, Job(coroutineContext.job))
            }
        } ?: run {
            Intention(job = Job(coroutineContext.job)) // TODO(Double-check, i'm not sure)
        }

        tryPut(nextIntention)
        return nextIntention
    }

    override suspend fun stepNextIntention() {
        continuations.tryReceive().getOrNull()?.let {
            println("Executing stepNextIntention")
            it()
        }
    }



}

