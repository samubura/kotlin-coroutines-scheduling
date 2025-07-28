package agent.examples

import agent.AchieveEvent
import agent.Agent
import agent.BasicMapEnvironment
import agent.EnvironmentContext
import agent.Plan
import agent.agent
import agent.environment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

suspend fun main() = supervisorScope {
    val pinger = Agent(
        "Pinger",
        listOf(
            Plan("start") {
                val agent = kotlin.coroutines.coroutineContext.agent
                val env = kotlin.coroutines.coroutineContext.environment as BasicMapEnvironment
                val x = 0
                env.set("ping", x)
                agent.say("Ping ${x}!")
            },
            Plan("+pong") {
                val agent = kotlin.coroutines.coroutineContext.agent
                val env = kotlin.coroutines.coroutineContext.environment as BasicMapEnvironment
                delay(500)
                agent.beliefs["pong"]?.let {
                    val x = it as Int + 1
                    env.set("ping", x)
                    agent.say("Ping ${x}!")
                }
            }
        ),
        listOf(
            AchieveEvent("start")
        )
    )

    val ponger = Agent(
        "Ponger",
        listOf(
            Plan("+ping") {
                val agent = kotlin.coroutines.coroutineContext.agent
                val env = kotlin.coroutines.coroutineContext.environment as BasicMapEnvironment
                delay(500)
                agent.beliefs["ping"]?.let {
                    val x = it as Int + 1
                    env.set("pong", x)
                    agent.say("Pong ${x}!")
                }
            }
        )
    )

    val agents = listOf(pinger, ponger)
    val env = BasicMapEnvironment(agents)

    val masContext = coroutineContext + EnvironmentContext(env)

    agents.forEach { agent ->
        launch (masContext) {
            agent.run()
        }
    }
}