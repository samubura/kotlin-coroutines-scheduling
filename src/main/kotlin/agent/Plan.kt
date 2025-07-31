package agent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext


class Plan<Result : Any?>(val trigger : String, val body: suspend CoroutineScope.() -> Result) : suspend (CoroutineScope) -> Result {
    override suspend fun invoke(scope: CoroutineScope): Result = scope.body()
}

// TODO revisit this generics
class PlanContext<Result: Any?>(val args : List<Any?> = emptyList(), val completion: CompletableDeferred<Result> = CompletableDeferred<Result>()) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<PlanContext<*>> = Key
    companion object Key : CoroutineContext.Key<PlanContext<*>>
}


//TODO plan hierarchy to distinghuish the matching events (instead of using the trigger string)