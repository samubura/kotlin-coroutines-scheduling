package agent

class Plan<T : Any?>(val trigger : String, val body: suspend() -> T) : suspend () -> T {
    override suspend fun invoke(): T  = body()
}