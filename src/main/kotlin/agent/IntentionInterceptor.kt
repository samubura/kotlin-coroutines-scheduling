package agent

import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

object IntentionInterceptor : ContinuationInterceptor {

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {

        val agent = continuation.context[AgentContext]!!.agent
        //agent.say("coroutine started")
        return object : Continuation<T> {
            override val context: CoroutineContext = continuation.context

            override fun resumeWith(result: Result<T>) {
                agent.continuations.trySend {
                    //agent.say("resuming")
                    continuation.resumeWith(result)
                    //agent.say("suspending")
                }
                agent.events.trySend(StepEvent)
            }
        }
    }

    override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
        // val agent =  continuation.context[AgentContext]!!.agent
        //agent.say("coroutine completed")
    }

    override val key: CoroutineContext.Key<*> = ContinuationInterceptor

}