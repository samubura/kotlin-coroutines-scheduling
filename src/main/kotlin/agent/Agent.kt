package agent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.CoroutineContext



class Agent (
    val name: String,
    val plans: List<Plan<Any?>>,
    val initialGoals: List<AchieveEvent<Any?>> = listOf(),
    val beliefs: MutableMap<String, Any> = mutableMapOf()
) {
    //TODO the channel is effective, but it does not allow for
    // setting priorities, or inspecting/canceling ongoing intentions
    // now intentions are executed on a first-come-first-served basis
    val intentions: Channel<() -> Unit> = Channel(Channel.Factory.UNLIMITED)

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
    suspend fun <T> achieve(planTrigger: String, vararg args : Any?) : T {
        val event = createAndSendAchieveEvent<T>(planTrigger, args)
        //say("Waiting subgoal to complete...")
        return event.completion.await()
    }

    /**
     * Adds an event to the agent's queue to achieve a goal and don't wait for it to complete.
     */
    suspend fun alsoAchieve(planTrigger: String, vararg args : Any) {
       createAndSendAchieveEvent<Any?>(planTrigger, args)
    }

    private suspend fun <T> createAndSendAchieveEvent(planTrigger: String, args: Array<out Any?>): AchieveEvent<T> {
        val event = AchieveEvent<T>(planTrigger, CompletableDeferred(), args.asList())
        events.send(event)
        return event
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
                say("Belief $beliefName already exists with value $value, ignoring.")
                return false
            }
        }
        beliefs[beliefName] = value
        return true
    }

    private fun matchPlan(event: Event) : Pair<Plan<Any?>, PlanContext<Any?>>? {
        when(event) {
            is AchieveEvent<*> -> {
                val plan = plans.find { it.trigger == event.planTrigger }
                    ?: run {
                        //say("No plan found for event: ${event.planTrigger}")
                        // TODO this now completely breaks the agent, but it should not...
                        event.completion.completeExceptionally(
                            IllegalStateException("No plan found for event: ${event.planTrigger}")
                        )
                        return null
                    } //No plan found for this event
                return plan to PlanContext(event.args, event.completion as CompletableDeferred<Any?>)  // TODO Brutto cast
            }
            is BeliefAddEvent<*> -> {
                //TODO if a belief is added manually, this is done twice (but ignored)
                // probably a good idea to distinguish between perceptions and manual belief addition??
                addBelief(event.beliefName, event.value)
                plans.find { it.trigger == "+${event.beliefName}"}?.let{
                    return it to PlanContext(listOf(event.value))
                }
                return null
            }
            is StepEvent -> {
                //TODO I'm not sure this is correct...
                intentions.tryReceive().getOrNull()?.let{
                    it()
                }
                return null
            }
            //TODO unnecessary, but useful now if I want to add events and not break compilation
            else -> {
                log("Unknown event type: $event")
                return null //No plan for unknown events
            }
        }
    }


    // TODO supervisorScope since child coroutines failing should not affect the parent or siblings
    suspend fun run() = supervisorScope {

        // Init the agent
        initialGoals.forEach{events.send(it)}

        //Run the loop
        while(true){
            //Handle an incoming event if available
            val event = events.receive()
            //TODO I tried to move this to the matchPlan function but it did not work, why?...
            // Probably because the launched coroutine needs to be a direct child of the current scope
            matchPlan(event)?.let { (plan, planContext) ->
                launch(context + planContext) {
                    val result = plan()
                    planContext.completion.complete(result) // Don't forget to complete the deferred!
//                    try{
//                        val result = async {plan()}.await()
//                        planContext.completion.complete(result) // Don't forget to complete the deferred!
//                    } catch (e: Exception){
//                        say("Error in plan ${plan.trigger}: ${e.message}")
//                        // TODO handle the error properly
//                        //planContext.completion.completeExceptionally(e)
//                        planContext.completion.complete(Unit)
//                    }
                }
            }
        }
    }
}

class AgentContext(val agent: Agent) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key
    companion object Key : CoroutineContext.Key<AgentContext>
}