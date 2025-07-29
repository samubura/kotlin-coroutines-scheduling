package agent.examples

import agent.AchieveEvent
import agent.Agent
import agent.BreakingEnvironment
import agent.EnvironmentContext
import agent.Plan
import agent.agent
import agent.args
import agent.environment
import agent.planContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.coroutineContext


private val test = Agent(
    "TestAgent",
    listOf(
        // TODO just to show that a plan breaking does not break the whole agent
        Plan("count"){
            val agent = coroutineContext.agent
            agent.say("Counting to 10...")
            for (i in 1..10) {
                delay(1000)
                agent.say("Count: $i")
            }
            agent.say("Done counting!")
        },
        Plan("start") {
            val agent = coroutineContext.agent
            val environment = coroutineContext.environment as BreakingEnvironment
//            agent.say("Starting subgoal...")
//            agent.achieve<Unit>("subgoalCatch")
//            agent.say("Subgoal completed")

            //TODO no exception is caught here, because the subgoal is recovered..
//            agent.say("Starting subgoal...")
//            try {
//                agent.achieve<Unit>("subgoalNoCatch")
//            } catch (e: Exception) {
//                agent.say("Caught an exception in subgoal: ${e.message}")
//            }
            agent.achieve<Unit>("subgoalNoCatch")
            agent.say("DONE")

        },
        //TODO This is the way to catch exceptions directly when they happen in actions
        // the logic to handle the error is within the same plan
        // it is easy, but requires explicit handling, sometimes we want to rely on the classic
        // BDI way to search for recovery plans automatically instead of manually declaring what to do
        Plan("subgoalCatch") {
            val agent = coroutineContext.agent
            val environment = coroutineContext.environment as BreakingEnvironment
            agent.say("I'm trying to perform an action with TryCatch...")
            try{
                environment.action()
            } catch (e: Exception) {
                agent.say("Caught an exception: ${e.message}")
                agent.achieve<Unit>("recover")
            }
        },
        //TODO This plan will break, ideally the BDI engine should catch it, try to recover
        // and complete with success the subgoal if it manages to find a recovery plan
        Plan("subgoalNoCatch") {
            val agent = coroutineContext.agent
            val environment = coroutineContext.environment as BreakingEnvironment
            agent.say("I'm trying to perform an action...")
            environment.action()
        },
        //TODO this is a fake recovery plan for testing purposes
        // the agent is hardcoded to always use this recovery regardless of what failed
        Plan("recover"){
            val agent = coroutineContext.agent
            agent.say("Recovering...")
            delay(1000)
            agent.say("Successfully recovered!")
        }
    ),
    listOf(
        AchieveEvent("start"),
        //AchieveEvent("count")
    )
)

private val counter = Agent(
    "Counter",
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
        AchieveEvent("count")
    )
)

suspend fun main() = supervisorScope {

    val agents = listOf(
        test,
        //counter
    )
    val env = BreakingEnvironment(agents)

    val masContext = coroutineContext + EnvironmentContext(env)

    agents.forEach { agent ->
        launch (masContext) {
            agent.run()
        }
    }
}