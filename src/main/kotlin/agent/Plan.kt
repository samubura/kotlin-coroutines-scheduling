package agent


class Plan<Result : Any?>(val trigger : String, val body: suspend() -> Result) : suspend () -> Result {
    override suspend fun invoke(): Result = body()
}

//TODO starting to work on a way to pass parameters to plans
//class Plan<Result : Any?>(val trigger : String, val body: suspend(PlanContext) -> Result) : suspend (PlanContext) -> Result {
//    override suspend fun invoke(planContext: PlanContext): Result = body(planContext)
//}
//
//
//TODO better to have everything in our custom context, or in the coroutineContext?
class PlanContext(val args: Sequence<Any>, val agent: Agent, val env : Environment)