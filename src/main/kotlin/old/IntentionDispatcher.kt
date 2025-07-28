package old

import agent.log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object PlanContextKey : CoroutineContext.Key<PlanContext>
@OptIn(ExperimentalUuidApi::class)
data class PlanContext(val planID: String,
                       private val intention : String?) : CoroutineContext.Element {
    val intentionId : String = intention ?: Uuid.random().toString()
    override val key: CoroutineContext.Key<*> get() = PlanContextKey
}

abstract class IntentionDispatcher(val plans : Sequence<Plan<Any?>>) : CoroutineDispatcher() {
    val intentions = mutableMapOf<String, ArrayDeque<Runnable>>()
    val suspendedIntentions = mutableSetOf<String>()
    val events = Channel<InternalEvent<Any?>>(Channel.UNLIMITED)

    private val lock = Mutex()
    private val intentionAvailable = Channel<Unit>(Channel.CONFLATED)

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        val planContext = context[PlanContextKey] ?: error("Coroutine is not a plan!")

        val intentionId = planContext.intentionId

        CoroutineScope(Dispatchers.Default).launch {
            lock.withLock {
                val stack = intentions.getOrPut(intentionId) { ArrayDeque() }
                stack.addLast(block)
                suspendedIntentions.remove(intentionId)
                signalIntentionAvailable()
            }
        }
    }

    suspend fun isIntentionAvailable() {
        intentionAvailable.receive()
    }

    protected abstract fun selectNextIntention(): String?

    fun step() {
        val intentionID = selectNextIntention() ?: return
        log("one step of intention: $intentionID")
        val runnable = intentions[intentionID]?.removeLastOrNull()
        runnable?.run()
    }


    fun markSuspended(intentionId: String){
        CoroutineScope(Dispatchers.Default).launch {
            lock.withLock {
                suspendedIntentions.add(intentionId)
            }
        }
    }

    suspend fun achieve(planID: String, completion: CompletableDeferred<*>, intentionId: String?){
        events.send(InternalEvent(planID, completion, intentionId) as InternalEvent<Any?>)
    }

    //TODO questa non mi piace, non credo dovrebbe esserci
    open fun cleanIntentions() {
        val toRemove = intentions.keys.filter { id ->
            intentions[id]?.isEmpty() == true
        }
        toRemove.forEach { id ->
            intentions.remove(id)
        }
    }

    private fun signalIntentionAvailable() {
        intentionAvailable.trySend(Unit).isSuccess
    }
}


class TrackingContinuationInterceptor(
    val dispatcher: IntentionDispatcher
) : ContinuationInterceptor {

    override val key: CoroutineContext.Key<*> = ContinuationInterceptor

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        val planContext = continuation.context[PlanContextKey] ?: return continuation
        val intentionId = planContext.intentionId
        log("intercepted $intentionId")
        return object: Continuation<T> {
            override val context: CoroutineContext = continuation.context

            override fun resumeWith(result: Result<T>) {
                log("resume $intentionId")
                dispatcher.dispatch(context, Runnable {
                    //agent.old.log("executing $intentionId")
                    continuation.resumeWith(result)
                    //TODO when sub-intention is completed for some reason
                    // maybe I don't need to mark it suspended?
                    log("suspended $intentionId")
                    dispatcher.markSuspended(intentionId)
                })
            }
        }
    }

    override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
        val intentionId = continuation.context[PlanContextKey]?.intentionId
        log("completed $intentionId")
        dispatcher.cleanIntentions()
    }
}