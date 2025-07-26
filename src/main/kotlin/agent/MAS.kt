package agent

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.collections.mapOf
import kotlin.coroutines.coroutineContext


val plans: Map<String, suspend () -> Unit> = mapOf(
    "hello" to {
        val agent =  coroutineContext.agent
        agent.say("Hello")
        delay(1000)
        agent.say("World!")
        agent.achieve("goodbye")
        agent.say("DONE")
    },
    "goodbye" to {
        val agent =  coroutineContext.agent
        agent.say("Goodbye")
        delay(1000)
        agent.say("See you later!")
    },
    "parallel" to {
        val agent =  coroutineContext.agent
        agent.say("before")
        delay(1000)
        agent.say("after")
    }
)



suspend fun main(): Unit = coroutineScope{

    listOf(
        Agent(
        "Bob", plans,
        listOf(
                AchieveEvent("hello")
            )
        ),
        Agent(
            "Carl", plans,
            listOf(
                AchieveEvent("hello")
            )
        ),
        Agent(
        "Alice", plans,
        listOf(
            AchieveEvent("parallel"),
            AchieveEvent("parallel")
            )
        )
    ).forEach { agent ->
        launch {
            agent.run()
        }
    }


    //launch { bob.run() }
    //launch { alice.run() }


}