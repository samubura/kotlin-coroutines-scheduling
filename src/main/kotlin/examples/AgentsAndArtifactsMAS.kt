package examples

import args
import environment
import environment.ArtifactBasedEnvironment
import examples.environment.Clock
import agent.AchieveEvent
import agent.Agent
import agent.Plan
import agent
import environment.EnvironmentContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope


private val initializer = Agent(
    "Initializer",
    listOf(
        Plan("start") {
            agent.say("Initializing environment with a clock artifact...")
            environment<ArtifactBasedEnvironment>().addArtifact(Clock())
        }
    ),
    listOf(
        AchieveEvent("start")
    )
)

private val user = Agent(
    "User",
    listOf(
        Plan("start") {
            environment<ArtifactBasedEnvironment>().getArtifact<Clock>()
                ?.let {
                    agent.say("Clock is available, starting to use it...")
                    // use the clock artifact
                    it.focus(agent)
                }
                ?: run {
                    // repeat until the clock is available
                    agent.alsoAchieve("start")
                }
        },
        Plan("+time"){
            agent.say("Current time: ${args[0]}")
        }
    ),
    listOf(
        AchieveEvent("start")
    )
)


suspend fun main() = supervisorScope {

    val agents = listOf(initializer, user)
    val env = ArtifactBasedEnvironment(setOf())
    env.init(this)

    val masContext = coroutineContext + EnvironmentContext(env)

    agents.forEach { agent ->
        launch (masContext) {
            agent.run()
        }
    }
}