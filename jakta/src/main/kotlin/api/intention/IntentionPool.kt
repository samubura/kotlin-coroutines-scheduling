package api.intention

import api.event.Event
import api.plan.Plan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job

interface IntentionPool {
    /**
     * Given an event, it returns the intention to execute it.
     */
    suspend fun CoroutineScope.nextIntention(event: Event.Internal): Intention

    fun getIntentionsSet(): Set<Intention>
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
    suspend fun stepIntention(event: Event.Internal.Step): Unit
}

class MutableIntentionPoolImpl: MutableIntentionPool {

    /** List of intentions currently managed by the agent. **/
    private val intentions: MutableSet<Intention> = mutableSetOf()

    // TODO(This needs to be invoked by someone)
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
                it
            }
        } ?: run {
            Intention(job = Job(currentCoroutineContext().job))
        }

        tryPut(nextIntention)
        return nextIntention
    }

    override suspend fun stepIntention(event: Event.Internal.Step) {
        intentions.find { it == event.intention }?.step()
    }

    override fun getIntentionsSet(): Set<Intention> = setOf(*intentions.toTypedArray())

}

