package examples

import agent.AchieveEvent
import agent.Agent
import agent.Plan
import agent
import args
import environment
import environment.EnvironmentContext
import examples.environment.BasicMapEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.supervisorScope


val countPlans : List<Plan<Any?>> = listOf(
    Plan("count"){
        agent.say("Counting to 50...")
        for (i in 1..50) {
            delay(1000)
            agent.say("Count: $i")
        }
        agent.say("Done counting!")
    },
)


suspend fun main() = supervisorScope {

    val agents = (1..1_000).map { Agent("counter$it", countPlans, listOf(AchieveEvent("count"))) }

    //val dispatcher = Dispatchers.Default.limitedParallelism(1)
    val dispatcher = newSingleThreadContext("TEST")
    val masContext = coroutineContext + dispatcher

    agents.forEach { agent ->
        launch (masContext) {
            agent.run()
        }
    }
}