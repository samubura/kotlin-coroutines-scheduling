package api.intention

import api.event.Event
import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

object IntentionInterceptor : ContinuationInterceptor {

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return object : Continuation<T> {
            override val context: CoroutineContext = continuation.context

            override fun resumeWith(result: Result<T>) {
                val currentIntention: Intention = context[Intention] as Intention
                currentIntention.resumeWith(continuation, result)
            }
        }
    }

    override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
        // No-op
    }

    override val key: CoroutineContext.Key<*> = ContinuationInterceptor
}