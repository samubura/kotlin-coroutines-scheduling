package agent

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class AgentContext(val agent: Agent) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key
    companion object Key : CoroutineContext.Key<AgentContext>
}

class Agent (
    val name: String,
    val plans: Map<String, suspend () -> Unit>,
    goals: List<AchieveEvent>
) {
    val intentions: Channel<() -> Unit> = Channel(Channel.Factory.UNLIMITED)

    val events: MutableList<Event> = goals.toMutableList()

    val context = IntentionInterceptor(intentions) +
            AgentContext(this)

    fun say(message: String){
        log("$name: $message")
    }

    // This is an Agent Action that can be used to add a new Event to the Agent's event queue
    // and wait for the corresponding goal to be achieved.
    suspend fun achieve(planTrigger: String) {
        val event = AchieveEvent(planTrigger)
        events.add(event)
        //say("Waiting subgoal to complete...")
        event.completion.await()
    }

    fun matchPlan(event: Event) : suspend () -> Unit {
        when(event) {
            is AchieveEvent -> {
                return plans[event.planTrigger] ?: {
                    throw IllegalStateException("No plan found for trigger: ${event.planTrigger}")
                }
            }
        }
    }

    suspend fun run() = coroutineScope {
        //say("Started...")
        while(true){
            //If there is a goal to be achieved, launch a plan for it
            if(events.isNotEmpty()){
                val event = events.removeFirst()
                when(event) {
                    is AchieveEvent -> {
                        //say("Achieving goal: ${event.planTrigger}")
                        // Launch a plan for the event
                        val plan = matchPlan(event)
                        launch(context) {
                            plan()
                            event.completion.complete(Unit) // TODO Important!
                        }
                    }
                }
            }
            //Execute one step of the next available intention, or wait indefinitely
            val continuation = intentions.receive()
            continuation()
        }
    }
}