package api.intention

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext
//
//object IntentionInterceptor : ContinuationInterceptor {
//    private val log =
//        Logger(
//            Logger.config,
//            "Interceptor",
//        )
//
//    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> = object : Continuation<T> {
//        override val context: CoroutineContext = continuation.context
//
//        override fun resumeWith(result: Result<T>) {
//            log.d { "Intercepting continuation with context: $context" }
//            val currentIntention: Intention = context[Intention] as Intention
//            currentIntention.enqueue { continuation.resumeWith(result) }
//        }
//    }
//
//    override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
//        // No-op
//    }
//
//    override val key: CoroutineContext.Key<*>
//        get() = ContinuationInterceptor.Key
//}


object IntentionDispatcher : CoroutineDispatcher() {

    private val log =
    Logger(
        Logger.config,
        "Interceptor",
    )

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        log.d { "Intercepting continuation with context: $context" }
            val currentIntention: Intention = context[Intention] as Intention
            currentIntention.enqueue { block.run() }
    }

}
