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
import examples.environment.FakeFetchArtifact
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope


private val fetcher: Agent = Agent(
    "Fetcher",
    listOf(
        Plan("fetch") {
            agent.say("Fetching data from the artifact...")
            val data = environment<ArtifactBasedEnvironment>().getArtifact<FakeFetchArtifact>()?.get()
            agent.say("Fetched data: $data")
            agent.say("Fetching data from the artifact...")
            val newData = environment<ArtifactBasedEnvironment>().getArtifact<FakeFetchArtifact>()?.getSuspend()
            agent.say("Fetched data: $newData")
            agent.say("Fetching data from the artifact...")
            val otherData = environment<ArtifactBasedEnvironment>().getArtifact<FakeFetchArtifact>()?.getAsync()
            agent.say("Fetched data: $otherData")
        },
        Plan("count"){
            agent.say("Counting to 20...")
            for (i in 1..20) {
                agent.say("Count: $i")
                // simulate some work
                delay(500)
            }
            agent.say("Done counting!")
        }
    ),
    listOf(
        AchieveEvent("fetch"),
        AchieveEvent("count")
    )
)


suspend fun main() = supervisorScope {

    val agents = listOf(fetcher)
    val env = ArtifactBasedEnvironment(setOf(FakeFetchArtifact()))
    env.init(this)

    val masContext = coroutineContext + EnvironmentContext(env)

    agents.forEach { agent ->
        launch (masContext) {
            agent.run()
        }
    }
}