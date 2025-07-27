package agent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext



class Agent (
    val name: String,
    val plans: Map<String, suspend () -> Unit>,
    val initialGoals: List<AchieveEvent>,
    val beliefs: MutableMap<String, Any> = mutableMapOf()
) {
    //TODO the channel is effective, but it does not allow for
    // setting priorities, or inspecting/canceling ongoing intentions
    // now intentions are executed on a first-come-first-served basis
    val intentions: Channel<() -> Unit> = Channel(Channel.Factory.UNLIMITED)

    //val events: MutableList<Event> = goals.toMutableList()
    val events: Channel<Event> = Channel(Channel.Factory.UNLIMITED)

    val context = IntentionInterceptor +
            AgentContext(this)

    fun say(message: String){
        log("$name: $message")
    }

    // This is an Agent Action that can be used to add a new Event to the Agent's event queue
    // and wait for the corresponding goal to be achieved.
    suspend fun achieve(planTrigger: String) {
        val event = AchieveEvent(planTrigger)
        events.send(event)
        //say("Waiting subgoal to complete...")
        event.completion.await()
    }

    suspend fun believe(beliefName: String, value: Any) {
        val event = BeliefAddEvent(beliefName, value)
        events.send(event)
    }

    fun matchPlan(event: Event) : Pair<suspend () -> Unit, CompletableDeferred<Unit>>? {
        when(event) {
            is AchieveEvent -> {
                val plan = plans[event.planTrigger]
                    ?: run {
                        say("No plan found for event: ${event.planTrigger}")
                        event.completion.completeExceptionally(
                            IllegalStateException("No plan found for event: ${event.planTrigger}")
                        )
                        return null
                    } //No plan found for this event
                return Pair(plan, event.completion)
            }
            is BeliefAddEvent<*> -> {
                if(beliefs.contains(event.beliefName)){
                    if(beliefs[event.beliefName] == event.value){
                        say("Belief ${event.beliefName} already exists with value ${event.value}, ignoring.")
                        return null // No need to add the belief, it already exists
                    }
                }
                beliefs[event.beliefName] = event.value
                plans["+${event.beliefName}"]?.let{
                    return Pair(it, CompletableDeferred()) // TODO this is a fake deferred, this won't be awaited
                }
                return null
            }
            is StepEvent -> {
                //say("I have a continuation to run...")
                intentions.tryReceive().getOrNull()?.let{ it()}
                return null
            }
            else -> {
                log("Unknown event type: $event")
                return null //No plan for unknown events
            }
        }
    }

    suspend fun run() = coroutineScope {

        // Init the agent
        initialGoals.forEach{events.send(it)}

        //Run the loop
        while(true){
            //Handle an incoming event if available
            val event = events.receive()
            //TODO I tried to move this to a suspend function but it did not work, why?
            matchPlan(event)?.let { (plan, completion) ->
                launch(context) {
                    plan()
                    completion.complete(Unit) // Don't forget to complete the deferred!
                }
            }
        }
    }
}

class AgentContext(val agent: Agent) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key
    companion object Key : CoroutineContext.Key<AgentContext>
}