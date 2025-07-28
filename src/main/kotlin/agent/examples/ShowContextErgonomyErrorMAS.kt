package agent.examples

import agent.BasicMapEnvironment
import agent.EnvironmentContext
import agent.agent
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

suspend fun main() = supervisorScope {

    val test = agent.Agent(
        "TestAgent",
        listOf(
            agent.Plan("hello") {
                //TODO This plan will break with exception because it is accessing the wrong
                // coroutineContext, since we are already in a coroutine
                val agent = coroutineContext.agent
                //TODO uncomment the next line to see that it is fixed, this is an ergonomy problem...
                //val agent =  kotlin.coroutines.coroutineContext.agent
                agent.say("Hello, world!")
            }

        ),
        listOf(
            agent.AchieveEvent("hello")
        )
    )

    val agents = listOf(test)
    val env = BasicMapEnvironment(agents)

    val masContext = coroutineContext + EnvironmentContext(env)

    agents.forEach { agent ->
        launch (masContext) {
            agent.run()
        }
    }
}