package agent

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.mapOf
import kotlin.coroutines.coroutineContext


val plans: Map<String, suspend () -> Unit> = mapOf(
    "hello" to {
        log("Hello")
        // Simulate some work
        delay(1000)
        log("World!")
        coroutineContext.agent.achieve("goodbye")
        delay(1000)
        log("Hello again!")
    },
    "goodbye" to {
        log("Goodbye")
        // Simulate some work
        delay(1000)
        log("See you later!")
    }
)



suspend fun main(): kotlin.Unit = coroutineScope{
    val bob = Agent("Bob", plans, listOf(AchieveEvent("hello")))
    //val alice = Agent("Alice", plans)


    launch {
        bob.run()
    }
    //launch { alice.run() }

}