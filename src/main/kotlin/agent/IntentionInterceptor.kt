package agent

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

class IntentionInterceptor(val intentionPool: Channel<() -> Unit>) : ContinuationInterceptor {

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {

        val name = continuation.context.agent.name
        log("${name}: coroutine started")
        return object : Continuation<T> {
            override val context: CoroutineContext = continuation.context

            override fun resumeWith(result: Result<T>) {
                intentionPool.trySend {
                    log("${name}: resuming")
                    continuation.resumeWith(result)
                    log("${name}: suspending")
                }
            }
        }
    }

    override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
        val name = continuation.context.agent.name
        log("${name}: coroutine completed")
    }

    override val key: CoroutineContext.Key<*> = ContinuationInterceptor

}