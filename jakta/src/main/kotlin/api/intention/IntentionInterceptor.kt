package api.intention

import api.event.Event
import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext


interface IntentionInterceptor : ContinuationInterceptor {
    val intentionPool : AddableIntentionPool
    val events: SendChannel<Event.Internal.Step>
}

class IntentionInterceptorImpl(
    override val intentionPool: AddableIntentionPool,
    override val events: SendChannel<Event.Internal.Step>
) : IntentionInterceptor {

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return object : Continuation<T> {
            override val context: CoroutineContext = continuation.context

            override fun resumeWith(result: Result<T>) {
                val currentIntention: Intention = context[Intention] as Intention
                println("Intercepted intention: $currentIntention")
                // Create a new Intention with the updated continuation
                currentIntention.resumeWith(continuation, result)
                // Send the Step event to let the agent process the continuation
                events.trySend(Event.Internal.Step(currentIntention))
            }
        }
    }

    override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
        // No-op
    }

    override val key: CoroutineContext.Key<*> = ContinuationInterceptor
}