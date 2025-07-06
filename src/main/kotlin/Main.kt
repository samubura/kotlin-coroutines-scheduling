import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.*

val plans : Sequence<Plan<Any?>> = sequenceOf(
    Plan("A") {
        repeat(3) {
            log("Plan A - step $it")
            delay(50)
            //achieve("B")
        }
        delay(50)

        log("Plan A DONE")
    },

    Plan("B") {
        log("Plan B - action")
        42
     },

    Plan("C") {
        repeat(3) {
            log("Plan C - step $it")
            delay(500)
        }
    }
)

// --- Usage example ---
fun main() = runBlocking {
    val dispatcher = RoundRobinDispatcher(plans)
    val interceptor = TrackingContinuationInterceptor(dispatcher)
    val scope = CoroutineScope(dispatcher + interceptor)

    launch(Dispatchers.Default) {
        while (true) {
            val event = withTimeoutOrNull(100) {
                dispatcher.events.receive()
            }
            if(event != null) {
                val plan = matchPlan(event, dispatcher.plans)
                if (plan != null) {
                    launchPlan(plan, event, scope)
                }
            }
            dispatcher.isIntentionAvailable()
            dispatcher.step()
        }
    }

    dispatcher.events.trySend(InternalEvent("A", intention = "Intention1"))
    //dispatcher.events.trySend(InternalEvent("C", intention = "Intention2"))

    println("Main done!")
}