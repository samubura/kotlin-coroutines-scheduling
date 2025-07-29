package agent.examples

import agent.AchieveEvent
import agent.Agent
import agent.BreakingEnvironment
import agent.EnvironmentContext
import agent.Intention
import agent.Plan
import agent.agent
import agent.args
import agent.log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.coroutineContext

private val intention = Intention("MAIN")

private val counter = Agent(
    "Counter",
    listOf(
        Plan("count"){
            val agent = coroutineContext.agent
            agent.say("Counting to 10...")
            agent.alsoAchieve("alsoCount") // This will live on in a different intention
            for (i in 1..10) {
                delay(1000)
                agent.say("Count: $i")
                agent.achieve<Unit>("alsoCount") // This will die when the intention is dropped
            }
            agent.say("Done counting!")
        },
        Plan("alsoCount"){
            val agent = coroutineContext.agent
            agent.say("alsoCounting to 10...")
            for (i in 1..10) {
                delay(1000)
                agent.say("alsoCount: $i")
            }
            agent.say("Done counting!")
        },
        Plan("drop"){
            val agent = coroutineContext.agent
            agent.say("Wait some time...")
            delay(coroutineContext.args[0] as Long)
            agent.say(agent.intentions.map {it.intention}.toString())
            agent.dropIntention(intention)
            agent.say("Dropped Intention!")
        },
    ),
    listOf(
        AchieveEvent("drop", args = listOf(2000L), intention = Intention("DROP")),
        AchieveEvent("count", intention = intention),
    )
)

private val secondCounter = Agent(
    "SecondCounter",
    listOf(
        Plan("count"){
            val agent = coroutineContext.agent
            agent.say("Counting to 10...")
            for (i in 1..10) {
                delay(1000)
                agent.say("Count: $i")
            }
            agent.say("Done counting!")
        },
    ),
    listOf(
        AchieveEvent("count"),
    )
)

suspend fun main(): Unit = supervisorScope {

    val agents = listOf(
        counter
    )

    val masContext = coroutineContext

    val agentJob = launch (masContext) {
        counter.run()
    }

    launch (masContext) {
        secondCounter.run()
    }

    launch {
        delay(5000) //TODO edit this to see different behaviors
        log("killing the counter")
        agentJob.cancel()
    }
}