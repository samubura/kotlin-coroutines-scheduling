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

    /**
     * Logs a message with the agent's name.
     */
    fun say(message: String){
        log("$name: $message")
    }


    /**
     * Adds an event to the agent's queue to achieve a goal and suspends until the goal is achieved.
     */
    suspend fun achieve(planTrigger: String) {
        val event = AchieveEvent(planTrigger)
        events.send(event)
        //say("Waiting subgoal to complete...")
        event.completion.await()
    }

    /**
     * Adds an event to the agent's queue to add a belief
     */
    suspend fun believe(beliefName: String, value: Any) {
        val added = addBelief(beliefName, value)
        if(added) {
            val event = BeliefAddEvent(beliefName, value)
            events.send(event)
        }
    }

    private fun addBelief(beliefName: String, value: Any) : Boolean {
        if(beliefs.contains(beliefName)){
            if(beliefs[beliefName] == value){
                say("Belief ${beliefName} already exists with value ${value}, ignoring.")
                return false
            }
        }
        beliefs[beliefName] = value
        return true
    }


    private fun matchPlan(event: Event) : Pair<suspend () -> Unit, CompletableDeferred<Unit>>? {
        when(event) {
            is AchieveEvent -> {
                val plan = plans[event.planTrigger]
                    ?: run {
                        say("No plan found for event: ${event.planTrigger}")
                        // TODO this now completely breaks the agent, but it should not...
                        event.completion.completeExceptionally(
                            IllegalStateException("No plan found for event: ${event.planTrigger}")
                        )
                        return null
                    } //No plan found for this event
                return Pair(plan, event.completion)
            }
            is BeliefAddEvent<*> -> {
                //TODO if a belief is added manually, this is done twice (but ignored)
                // probably a good idea to distinguish between perceptions and manual belief addition??
                addBelief(event.beliefName, event.value)
                plans["+${event.beliefName}"]?.let{
                    return Pair(it, CompletableDeferred()) //TODO this is a fake deferred nobody is awaiting it
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
            //TODO I tried to move this to the matchPlan function but it did not work, why?...
            // Probably because the launched coroutine needs to be a direct child of the current scope
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