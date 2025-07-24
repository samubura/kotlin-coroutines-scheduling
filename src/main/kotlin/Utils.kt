import PlanContext
import PlanContextKey
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.coroutineContext

fun log(message: String) {
    println("${Thread.currentThread()} - $message")
}



suspend fun achieve(planID: String, completion: CompletableDeferred<Unit> = CompletableDeferred<Unit>()) {
    log("I want to agent.achieve plan $planID")
    val intentionId = coroutineContext[PlanContextKey]?.intentionId
    val interceptor = coroutineContext[ContinuationInterceptor] as TrackingContinuationInterceptor
    interceptor.dispatcher.achieve(planID, completion, intentionId)
    completion.await()
    log("Plan $planID achieved, continuing...")
}

fun <T> matchPlan(event : InternalEvent<T>, plans: Sequence<Plan<T>>): Plan<T>? {
    return plans.filter {it.id == event.planTrigger}.firstOrNull()
}

fun <T> launchPlan(plan: Plan<T>, event: InternalEvent<T>, scope: CoroutineScope) {
    scope.launch(PlanContext(plan.id, event.intentionId)) {
        log("Launching plan ${plan.id}")
        val result = plan.body()
        event.completion.complete(result) //TODO this is not releasing the intention, why?
        //TODO i suspect this has something to do with cleaning up the intention
        log("Plan ${plan.id} completed")
    }
}

