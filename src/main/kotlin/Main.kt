import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.*

val plans = sequenceOf(
    Plan("A") {
        repeat(3) {
            log("Plan A - step $it")
            delay(500)
        }
    },
    Plan("B") { repeat(3) {
        log("Plan B - step $it")
        delay(500)
    } },
    Plan("C") { repeat(3) {
        log("Plan C - step $it")
        delay(500)
    } }
)

// --- Usage example ---
fun main() = runBlocking {
    val dispatcher = RoundRobinDispatcher(plans)
    val interceptor = TrackingContinuationInterceptor(dispatcher)
    val scope = CoroutineScope(dispatcher + interceptor)

    scope.launch(PlanContext("0", "test")) {
        repeat(3) {
            log("Plan 0 - step $it")
            delay(500)
        }
    }

    launch(Dispatchers.Default) {
        while (true) {
            dispatcher.isIntentionAvailable()
            dispatcher.step()
        }
    }

    println("Main done!");
}