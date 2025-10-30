package api.intention

import api.event.Event
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

object IntentionInterceptor : ContinuationInterceptor {

    private val log = Logger(
        Logger.config,
        "Interceptor",
    )


    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {

        return object : Continuation<T> {
            override val context: CoroutineContext = continuation.context

            override fun resumeWith(result: Result<T>) {
                log.d {"Intercepting continuation with context: $context"}
                val currentIntention: Intention = context[Intention] as Intention
                currentIntention.enqueue { continuation.resumeWith(result) }
            }
        }
    }

    override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
        // No-op
    }

    override val key: CoroutineContext.Key<*>
        get() = ContinuationInterceptor.Key
}