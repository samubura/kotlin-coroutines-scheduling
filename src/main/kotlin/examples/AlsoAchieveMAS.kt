package examples

import agent.AchieveEvent
import agent.Agent
import agent.Plan
import agent
import environment.BasicMapEnvironment
import environment.EnvironmentContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.coroutineContext

private val myAgent = Agent(
    "TestAgent",
    listOf(
        Plan("start") {
            agent.say("Starting...")
            agent.alsoAchieve("task1")
            agent.alsoAchieve("task2")
            agent.say("Tasks started, but I'm not waiting for them to finish.")
            agent.say("I can do other things while tasks are running.")
            repeat(10) {
                agent.say("Doing something else... $it")
                delay(200)
            }
            agent.say("I'm done, but maybe other tasks are still running")

        },
        Plan("task1") {
            agent.say("Executing task 1...")
            delay(1000)
            agent.say("Task 1 completed!")
        },
        Plan("task2") {
            agent.say("Executing task 2...")
            delay(1000)
            agent.say("Task 2 completed!")
        }

    ),
    listOf(
        AchieveEvent("start")
    )
)


suspend fun main() = supervisorScope {

    val agents = listOf(myAgent)
    val env = BasicMapEnvironment(agents)

    val masContext = coroutineContext + EnvironmentContext(env)

    agents.forEach { agent ->
        launch (masContext) {
            agent.run()
        }
    }
}