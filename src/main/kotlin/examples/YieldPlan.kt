package examples

import agent.AchieveEvent
import agent.Agent
import agent.Plan
import agent
import agent.StepEvent
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.yield
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


//TODO this is a workaround to yield a plan
// essentially allowing to break a plan into smaller "atomic" steps
// WARNING this is inefficient because the agent will run several empty reasoning cycles
suspend fun wrongYieldPlan() = coroutineScope {
     launch {}
    // async {}.await() is equivalent
}


//TODO is the right way to yield a plan?
// essentially forcing the coroutine to suspend and adding the continuation manaually
// to the queue + adding the event to continue the plan
// WARNING this has the same drawbacks as the previous one in terms of "wasted" cycles... but feels less of a hack
// !! This is the same code used in the IntentionInterceptor
suspend fun yieldPlan() = coroutineScope {
    suspendCoroutine { continuation ->
        agent.continuations.trySend {
            continuation.resume(Unit)
        }
        agent.events.trySend(StepEvent)

    }
}

//TODO it can be used to implement jason-like internal actions
// i.e. executing one step of a plan per reasoning cycle
// without this if I just use regular agent.say(message) all the prints will be executed within the same reasoning cycle
suspend fun oneStepPrint(message: String) = coroutineScope {
    agent.say(message)
    //TODO normal Kotlin yield won't work, because it is optimized and realizes it can be skipped
    // i.e. the plan will not suspend
    //yield()
    //wrongYieldPlan()
    yieldPlan()
}

private val yielder: Agent = Agent(
    "Yield",
    listOf(
        Plan("goal-1") {
            repeat(5){
                oneStepPrint("Yielding for goal-1")
            }

        },
        Plan("goal-2") {
            repeat(5){
                oneStepPrint("Yielding for goal-2")
            }
        },
        Plan("goal-3") {
            repeat(5){
                oneStepPrint("Yielding for goal-3")
            }
        },

    ),
    listOf(
        AchieveEvent("goal-1"),
//        AchieveEvent("goal-2"),
//        AchieveEvent("goal-3"),
    )
)


suspend fun main() = supervisorScope {

    val agents = listOf(yielder)

    val masContext = coroutineContext

    agents.forEach { agent ->
        launch (masContext) {
            supervisorScope {
                agent.init()
                while(true) {
                    agent.step(this)
                    //delay(500)
                }
            }
            //agent.run()

        }
    }
}