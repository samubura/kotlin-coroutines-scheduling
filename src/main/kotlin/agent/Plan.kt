package agent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext


open class Plan<Result>(val trigger : String, val body: suspend CoroutineScope.() -> Result) : suspend (CoroutineScope) -> Result {
    override suspend fun invoke(scope: CoroutineScope): Result = scope.body()
}

//TODO use the plan hierarchy to distinghuish the matching events (instead of using the trigger string)

class BeliefAddPlan <Result>(trigger: String, body: suspend CoroutineScope.() -> Result) : Plan<Result>(trigger, body)

class AchievePlan <Result>(trigger: String, body: suspend CoroutineScope.() -> Result) : Plan<Result>(trigger, body)

class AchieveFailedPlan <Result>(trigger: String, body: suspend CoroutineScope.() -> Result) : Plan<Result>(trigger, body)

class TestPlan <Result>(trigger: String, body: suspend CoroutineScope.() -> Result) : Plan<Result>(trigger, body)

class TestFailed <Result>(trigger: String, body: suspend CoroutineScope.() -> Result) : Plan<Result>(trigger, body)

// TODO revisit this generics
class PlanContext<Result: Any?>(val args : List<Any?> = emptyList(), val completion: CompletableDeferred<Result> = CompletableDeferred<Result>()) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<PlanContext<*>> = Key
    companion object Key : CoroutineContext.Key<PlanContext<*>>
}
