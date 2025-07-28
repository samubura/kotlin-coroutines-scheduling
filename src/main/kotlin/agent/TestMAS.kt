package agent

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.collections.mapOf
import kotlin.coroutines.coroutineContext


val plans: Map<String, suspend () -> Unit> = mapOf(
    "hello" to {
        val agent = coroutineContext.agent
        agent.say("Hello")
        delay(1000)
        agent.say("World!")
        agent.achieve("goodbye")
        agent.say("DONE")
    },
    "goodbye" to {
        val agent = coroutineContext.agent
        agent.say("Goodbye")
        delay(1000)
        agent.say("See you later!")
    },
    "break" to {
        val agent = coroutineContext.agent
        agent.achieve("xyz")
    },
    "parallel" to {
        val agent = coroutineContext.agent
        agent.say("before")
        delay(1000)
        agent.say("after")
    },
    "+x" to {
        val agent = coroutineContext.agent
        agent.say("Now I believe that x is ${agent.beliefs["x"]}")
    }
)



// Important -> supervisorScope allows independent execution of agents (one fails, others continue)
suspend fun main(): Unit = supervisorScope{

    val agents = listOf(
        Agent(
        "Bob", plans,
        listOf(
                AchieveEvent("hello")
            )
        ),
//        Agent(
//            "Carl", plans,
//            listOf(
//                AchieveEvent("hello")
//            )
//        ),
//        Agent(
//        "Alice", plans,
//        listOf(
//            AchieveEvent("parallel"),
//            AchieveEvent("parallel")
//            )
//        )
    )
    agents.forEach { agent ->
        launch {
            agent.run()
        }
    }


}