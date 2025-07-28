package agent.examples

import agent.AchieveEvent
import agent.Agent
import agent.BasicMapEnvironment
import agent.EnvironmentContext
import agent.Plan
import agent.agent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.coroutineContext
import kotlin.random.Random


private val test = Agent(
    "TestAgent",
    listOf(
        Plan("start") {
            val agent = coroutineContext.agent
            agent.say("Starting...")
            val x = agent.achieve<Int>("x")
            val y = agent.achieve<Int>("y")
            agent.say("Result: $x + $y = ${x + y}")
        },
        Plan("x") {
            val agent = coroutineContext.agent
            agent.say("Fetching x...")
            delay(1000)
            Random.nextInt(1,100)
            //TODO even if the old.achieve is typed, you have no guarantee that the result will be of that type
            // and the cast will break. Try uncomment the next line to see the error
            //"HELLO"
        },
        Plan("y") {
            val agent = coroutineContext.agent
            agent.say("Fetching y...")
            delay(1000)
            Random.nextInt(1,100)
        }
    ),
    listOf(
        AchieveEvent("start")
    )
)


suspend fun main() = supervisorScope {

    val agents = listOf(test)
    val env = BasicMapEnvironment(agents)

    val masContext = coroutineContext + EnvironmentContext(env)

    agents.forEach { agent ->
        launch (masContext) {
            agent.run()
        }
    }
}