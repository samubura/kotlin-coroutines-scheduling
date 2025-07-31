package examples

import agent.AchieveEvent
import agent.Agent
import agent.Plan
import agent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.coroutineContext


val plans: List<Plan<Any?>> = listOf(
    Plan("hello") {
        agent.say("Hello")
        delay(1000)
        agent.say("World!")
        agent.achieve<Unit>("goodbye")
        agent.say("DONE")
    },
    Plan("goodbye") {
        agent.say("Goodbye")
        delay(1000)
        agent.say("See you later!")
    },
    Plan("break") {
        agent.achieve("xyz")
    },
    Plan("parallel") {
        agent.say("before")
        delay(1000)
        agent.say("after")
    },
    Plan("+x") {
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
//            "Carl", old.plans,
//            listOf(
//                AchieveEvent("hello")
//            )
//        ),
//        Agent(
//        "Alice", old.plans,
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