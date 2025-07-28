package agent

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.coroutineContext


val pinger = Agent("Pinger",
    mapOf(
        "start" to {
            val agent = coroutineContext.agent
            val env = coroutineContext.environment as BasicMapEnvironment
            val x = 0
            env.set("ping", x)
            agent.say("Ping ${x}!")
        },
        "+pong" to {
            val agent = coroutineContext.agent
            val env = coroutineContext.environment as BasicMapEnvironment
            delay(500)
            agent.beliefs["pong"]?.let{
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


val ponger = Agent("Ponger",
    mapOf(
        "+ping" to {
            val agent = coroutineContext.agent
            val env = coroutineContext.environment as BasicMapEnvironment
            delay(500)
            agent.beliefs["ping"]?.let{
                val x = it as Int + 1
                env.set("pong", x)
                agent.say("Pong ${x}!")
            }
        }
    ),
    listOf()
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