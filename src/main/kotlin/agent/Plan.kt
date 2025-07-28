package agent

import kotlinx.coroutines.CompletableDeferred
import kotlin.coroutines.CoroutineContext


class Plan<Result : Any?>(val trigger : String, val body: suspend() -> Result) : suspend () -> Result {
    override suspend fun invoke(): Result = body()
}

class PlanContext<Result: Any?>(val args : Sequence<Any?> = emptySequence(), val completion: CompletableDeferred<Result> = CompletableDeferred<Result>()) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key
    companion object Key : CoroutineContext.Key<PlanContext<*>>
}