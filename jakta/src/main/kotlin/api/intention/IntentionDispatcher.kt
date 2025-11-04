package api.intention

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
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

/**
 * A [ContinuationInterceptor] that wraps another interceptor and implements [Delay] by delegating
 * to the wrapped interceptor. It is necessary that interceptors used in tests, with one of the
 * [TestDispatcher]s, propagate delay like this in order to work with the delay skipping that those
 * dispatchers perform.
 */
// TODO(b/263369561): avoid InternalCoroutinesApi - it is not expected that Delay gain a method but
// if it ever did this would have potential runtime crashes for tests. Medium term we will leave
// this dependency as the chance of this faulting is low, and it should only effect tests until next
// recompile if it did fault.

@OptIn(InternalCoroutinesApi::class)
abstract class DelayPropagatingContinuationInterceptorWrapper(
    wrappedInterceptor: ContinuationInterceptor
) :
    AbstractCoroutineContextElement(ContinuationInterceptor),
    ContinuationInterceptor,
    // Coroutines will internally use the Default dispatcher as the delay if the
    // ContinuationInterceptor does not implement Delay.
    Delay by ((wrappedInterceptor as? Delay)
        ?: error(
            "wrappedInterceptor of DelayPropagatingContinuationInterceptorWrapper must implement Delay"
        ))


class IntentionInterceptor(wrappedInterceptor: ContinuationInterceptor) : DelayPropagatingContinuationInterceptorWrapper(wrappedInterceptor) {

    private val log =
    Logger(
        Logger.config,
        "Interceptor",
    )

//    override fun dispatch(context: CoroutineContext, block: Runnable) {
//        log.d { "Intercepting continuation with context: $context" }
////            val currentIntention: Intention = context[Intention] as Intention
////            currentIntention.enqueue { block.run() }
//    }

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> = object : Continuation<T> {
        override val context: CoroutineContext = continuation.context

        override fun resumeWith(result: Result<T>) {
            log.d { "Intercepting continuation with context: $context" }
            val currentIntention: Intention = context[Intention] as Intention
            currentIntention.enqueue { continuation.resumeWith(result) }
        }
    }

}
