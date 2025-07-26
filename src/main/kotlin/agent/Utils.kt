package agent

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

fun log(message: String) {
    println("${Thread.currentThread()} - $message")
}

val CoroutineContext.agent: Agent
    get() = get(AgentContext)?.agent ?: error("Not inside an AgentContext!")
