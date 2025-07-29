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


//TODO this is a bad example, I'm keeping it now just to remember that
// there is also the CoroutineExceptionHandler, but the implementation now does not use it.

private val test = Agent(
    "TestAgent",
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
        Plan("start") {
            val agent = coroutineContext.agent
            val environment = coroutineContext.environment as BreakingEnvironment
            agent.say("Starting subgoal...")
            agent.achieve<Unit>("subgoalCatch")
            agent.say("Starting subgoal...")

            //TODO catching exceptions in subgoals won't usually work... but now it does thanks to the CoroutineExceptionHandler
            // propagating the exceptions in a controlled way...
            try {
                agent.achieve("subgoalNoCatch")
            } catch(e: Exception) {
                agent.say("Caught an exception in subgoalNoCatch: ${e.message}")
            }
            agent.achieve<Unit>("subgoalNoCatch")
            agent.say("DONE")

        },
        Plan("subgoalCatch") {
            val agent = coroutineContext.agent
            val environment = coroutineContext.environment as BreakingEnvironment
            agent.say("I'm trying to perform an action with TryCatch...")
            try{
                environment.action()
            } catch (e: Exception) {
                agent.say("Caught an exception: ${e.message}")
            }
        },
        Plan("subgoalNoCatch") {
            val agent = coroutineContext.agent
            val environment = coroutineContext.environment as BreakingEnvironment
            agent.say("I'm trying to perform an action...")
            environment.action()
        },
        Plan("recover"){
            val agent = coroutineContext.agent
            agent.say("Recovering...")
            delay(1000)
            agent.say("Successfully recovered!")
        }
    ),
    listOf(
        AchieveEvent("start"), AchieveEvent("count")
    )
)


suspend fun main() = supervisorScope {

    val agents = listOf(test)
    val env = BreakingEnvironment(agents)

    //TODO This is one way to handle exceptions..
    // I guess it makes more sense to make it part of the agent's context, rather than the MAS context..
    // Anyway, this is not ideal, the CoroutineExceptionHandler is not really designed to recover from exceptions,
    // and is designed as a "last resort" to handle uncaught exceptions in coroutines.
    val handler = CoroutineExceptionHandler(
        { context, exception ->
            val agent = context.agent
            agent.say("EXCEPTION! ${exception.message}")
            //TODO attempt to recover from the exception

            //TODO THIS IS WORKING BUT IT IS VERY BAD AND TYPES ARE EXPLODING
            // also what is
            launch {
                val result = agent.achieve<Unit>("recover", context.args)
                (context.planContext.completion as CompletableDeferred<Unit>).complete(result)
            }
            //context.planContext.completion.completeExceptionally(exception)
        }
    )

    val masContext = coroutineContext + EnvironmentContext(env) + handler

    agents.forEach { agent ->
        launch (masContext) {
            agent.run()
        }
    }
}