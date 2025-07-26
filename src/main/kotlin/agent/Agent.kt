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
    //TODO the channel is effective, but it does not allow for
    // setting priorities, or inspecting/canceling ongoing intentions
    // now intentions are executed on a first-come-first-served basis
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

    private fun matchPlan(event: Event) : suspend () -> Unit {
        when(event) {
            is AchieveEvent -> {
                return plans[event.planTrigger] ?: {
                    //TODO handle this better
                    throw IllegalStateException("No plan found for trigger: ${event.planTrigger}")
                }
            }
        }
    }

    suspend fun run() = coroutineScope {
        //say("Started...")
        while(true){
            //Handle an incoming event if available
            if(events.isNotEmpty()){
                val event = events.removeFirst()
                when(event) {
                    is AchieveEvent -> {
                        //say("Achieving goal: ${event.planTrigger}")
                        val plan = matchPlan(event)
                        launch(context) {
                            plan()
                            event.completion.complete(Unit) //Don't forget to complete the deferred!
                        }
                    }
                }
            }
            //Execute one step of the next available intention, or wait indefinitely
            //TODO an agent that has nothing to do now will block forever
            // the agent should be allowed to perceive new events while waiting
            // for the next intention to be available.. but how?
            // (without busy waiting, of course)
            val continuation = intentions.receive()
            continuation()
        }
    }
}