package agent

import kotlin.coroutines.CoroutineContext

fun log(message: String) {
    println("${Thread.currentThread()} - $message")
}

val CoroutineContext.agent: Agent
    get() = get(AgentContext)?.agent ?: error("Not inside an AgentContext!")

val CoroutineContext.environment: Environment
    get() = get(EnvironmentContext)?.environment ?: error("Not inside an EnvironmentContext!")
