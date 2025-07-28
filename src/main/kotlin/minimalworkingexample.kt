import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import old.log
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext


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
    log("Waiting subgoal to complete...")
    deferred.await()
}

suspend fun subplan(completion: CompletableDeferred<Unit>){
    log("subgoal is working")
    delay(1000);
    completion.complete(Unit)
    log("subgoal achieved!")
}

suspend fun plan(name: String) {
    repeat(3) {
        log("$name $it")
        //delay(100)
        log("$name achieving...")
        achieve()
        log("$name completed!")
    }
}

suspend fun noSubGoalPlan(name: String) {
    repeat(20) {
        log("$name $it")
        delay(50)
    }
    log("$name completed!")
}


fun main() {
    var step = 0;
    val interceptor = MarmellataInterceptor
    runBlocking {

        //basically this would be the agent body, launching initial old.plans on the interceptor
        launch( interceptor) {
            launch {plan("A")}
            launch {plan("B")}
            launch {noSubGoalPlan("C")}
        }

        //Main execution loop
        launch(Dispatchers.Default){
            while(true){
                log("Step ${step++}");
                //if there is a subgoal to agent.old.achieve, launch the corresponding subplan
                if(deferredList.isNotEmpty()){
                    val deferred = deferredList.removeFirst()
                    launch(interceptor){
                        subplan(deferred)
                    }
                }
                val continuation = channel.receive()
                continuation()

                //if there is an intention available to continue, run it
//                val continuation = channel.tryReceive()
//                if(continuation.isSuccess){
//                    agent.old.log("Continuation available, running...")
//                    continuation.getOrNull()?.invoke() //run the continuation
//                } else {
//                    agent.old.log("No continuation available...")
//                    delay(200)
//                }
                //fake delay for easy read
                //delay(1000)
            }
        }

    }
}

object MarmellataInterceptor : ContinuationInterceptor {

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        log("coroutine started")
        return object : Continuation<T> {
            override val context: CoroutineContext = continuation.context

            override fun resumeWith(result: Result<T>) {
                channel.trySend {
                    log("resuming")
                    continuation.resumeWith(result)
                    log("suspending")
                }
            }
        }
    }

    override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
        //agent.old.log("coroutine completed")
    }

    override val key: CoroutineContext.Key<*> = ContinuationInterceptor

}
