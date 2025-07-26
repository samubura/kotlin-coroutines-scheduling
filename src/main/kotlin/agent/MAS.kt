package agent

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.mapOf
import kotlin.coroutines.coroutineContext


val plans: Map<String, suspend () -> Unit> = mapOf(
    "hello" to {
        val agent =  coroutineContext.agent
        agent.say("Hello")
        // Simulate some work
        delay(1000)
        agent.say("World!")
        agent.achieve("goodbye")
        agent.say("DONE")
    },
    "goodbye" to {
        val agent =  coroutineContext.agent
        agent.say("Goodbye")
        // Simulate some work
        delay(1000)
        agent.say("See you later!")
    },
    "test" to {
        val agent =  coroutineContext.agent
        agent.say("Test")
    }
)



suspend fun main(): kotlin.Unit = coroutineScope{
    val bob = Agent("Bob", plans, listOf(AchieveEvent("hello")))
    val alice = Agent("Alice", plans, listOf(AchieveEvent("goodbye")))


    launch { bob.run() }
    //launch { alice.run() }

}