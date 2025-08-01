package examples

import agent.AchieveEvent
import agent.Agent
import agent.Plan
import agent
import args
import environment.EnvironmentContext
import examples.environment.BasicMapEnvironment
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope


private val fibonacci = Agent(
    "Fibonacci",
    listOf(
        Plan("start") {
            val n = args[0]
            agent.say("Calculating Fibonacci of $n")
            val result = agent.achieve<Int>("fibonacci", n)
            agent.say("Fibonacci of $n is $result")
        },
        Plan("fibonacci") {
            val n = args[0] as Int
            if (n <= 1) return@Plan n
            val a = agent.achieve<Int>("fibonacci", n - 1)
            val b = agent.achieve<Int>("fibonacci", n - 2)
            return@Plan a + b
        }
    ),
    listOf(
        AchieveEvent("start", args = listOf(10))
    )
)

suspend fun main() = supervisorScope {

    val agents = listOf(fibonacci)
    val env = BasicMapEnvironment(agents)

    val masContext = coroutineContext + EnvironmentContext(env)

    agents.forEach { agent ->
        launch (masContext) {
            agent.run()
        }
    }
}