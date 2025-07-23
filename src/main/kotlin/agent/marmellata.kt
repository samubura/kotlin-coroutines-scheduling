package agent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import log
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


val channel : Channel<() -> Unit> = Channel(Channel.Factory.UNLIMITED)

val deferredList : MutableList<CompletableDeferred<Unit>> = mutableListOf()

suspend fun a1() = coroutineScope {
    launch{println("A1")}.join()
}

suspend fun a2() = coroutineScope {
    launch{println("A2")}.join()
}

suspend fun achieve() {
    val deferred : CompletableDeferred<Unit> = CompletableDeferred()
    deferredList.add(deferred)
    deferred.await()
}

suspend fun subplan(completion: CompletableDeferred<Unit>){
    log("subgoal is working")
    delay(1000);
    completion.complete(Unit)
    log("subgoal completed!")
}

suspend fun plan(name: String) {
    coroutineScope {
        repeat(3) {
            log("$name $it")
            //delay(100)
            log("$name achieving...")
            achieve()
            log("$name achieved!")
        }
    }
}


fun main() {
    var step = 0;
    val interceptor = TestInterceptor
    runBlocking {

        //basically this would be the agent body, launching initial plans on the interceptor
        launch( interceptor) {
            launch {plan("A")}
            launch {plan("B")}
        }

        //Main execution loop
        launch(Dispatchers.Default){
            while(true){
                //if there is a subgoal to achieve, launch the corresponding subplan
                if(deferredList.isNotEmpty()){
                    launch(interceptor){
                        subplan(deferredList.removeFirst())
                    }
                }
                //if there is an intention available to continue, run it
                val continuation = channel.receive()
                log("Step ${step++}");
                //TODO THIS IS WRONG continuations are always running on the same main thread and end up starvating it
                // the interceptor overrides the dispatcher, meaning that we need both??
                // but how to run the continuation not in the same thread that is managing the agent lifecycle?
                // without losing the interception??
                continuation()
                //fake delay for easy read
                //delay(1000)
            }
        }

    }
}

object TestInterceptor : ContinuationInterceptor {

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        //log("coroutine started")
        return object : Continuation<T> {;
            override val context: CoroutineContext = CoroutineName("Intercepted")

            override fun resumeWith(result: Result<T>) {
                channel.trySend {
                    continuation.resumeWith(result)
                    val job = continuation.context.job;
                    //log("Active: ${job.isActive}, Completed:${job.isCompleted}")
                    log("suspending")
                }
            }
        }
    }

    override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
        //log("coroutine completed")
    }

    override val key: CoroutineContext.Key<*> = ContinuationInterceptor

}
