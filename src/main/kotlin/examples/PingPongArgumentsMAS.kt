package examples

import agent.AchieveEvent
import agent.Agent
import agent.Plan
import agent
import args
import environment
import environment.BasicMapEnvironment
import environment.BreakingEnvironment
import environment.EnvironmentContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.coroutineContext

private val pinger = Agent(
    "Pinger",
    listOf(
        Plan("start") {
            val env = environment<BasicMapEnvironment>()
            val x = 0
            env.set("ping", x)
            agent.say("Ping ${x}!")
        },
        Plan("+pong") {
            val env = environment<BasicMapEnvironment>()
            delay(500)
            // TODO the first argument of a belief update plan is the belief value
            val x = args[0] as Int + 1
            env.set("ping", x)
            agent.say("Ping ${x}!")
        }
    ),
    listOf(
        AchieveEvent("start")
    )
)

private val ponger = Agent(
    "Ponger",
    listOf(
        Plan("+ping") {
            val env = environment<BasicMapEnvironment>()
            delay(500)
            val x = args[0] as Int + 1
            env.set("pong", x)
            agent.say("Pong ${x}!")
        }
    )
)

suspend fun main() = supervisorScope {
    val agents = listOf(pinger, ponger)
    val env = BasicMapEnvironment(agents)

    val masContext = coroutineContext + EnvironmentContext(env)

    agents.forEach { agent ->
        launch (masContext) {
            agent.run()
        }
    }
}