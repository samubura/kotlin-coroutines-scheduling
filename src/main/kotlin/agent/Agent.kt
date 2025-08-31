package agent

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import log
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


class Agent (
    val name: String,
    val plans: List<Plan<Any?>>,
    val initialGoals: List<AchieveEvent<Any?>> = listOf(),
    val beliefs: MutableMap<String, Any?> = mutableMapOf()
) {
    //TODO the channel is effective, but it does not allow for
    // setting priorities, or inspecting/canceling ongoing intentions
    // now intentions are executed on a first-come-first-served basis
    val continuations: Channel<() -> Unit> = Channel(Channel.Factory.UNLIMITED)

    val intentions: MutableSet<IntentionContext> = mutableSetOf()

    val events: Channel<Event> = Channel(Channel.Factory.UNLIMITED)

    val agentContext = IntentionInterceptor +
            AgentContext(this)

    var stepNumber = 0L

    /**
     * Logs a message with the agent's name.
     */
    fun say(message: String){
        log("[Step: $stepNumber] $name: $message")
    }

    /**
     * Adds an event to the agent's queue to achieve a goal and suspends until the goal is achieved.
     */
    suspend fun <T> achieve(planTrigger: String, vararg args : Any?) : T {
        val intention = coroutineContext[IntentionContext]?.intention
        val event = createAndSendAchieveEvent<T>(planTrigger, args, intention)
        //say("Waiting subgoal to complete...")
        return event.completion.await()
    }

    /**
     * Adds an event to the agent's queue to achieve a goal and don't wait for it to complete.
     */
    suspend fun alsoAchieve(planTrigger: String, vararg args : Any) {
       createAndSendAchieveEvent<Any?>(planTrigger, args, null)
    }

    private suspend fun <T> createAndSendAchieveEvent(planTrigger: String, args: Array<out Any?>, intention: Intention?): AchieveEvent<T> {
        val event = AchieveEvent<T>(planTrigger, CompletableDeferred(), args.asList(), intention)
        events.send(event)
        return event
    }

    /**
     * Tries to add a belief, if successful sends a BeliefAddEvent to be handled by the agent
     */
    suspend fun believe(beliefName: String, value: Any) {
        val added = addBelief(beliefName, value)
        if(added) {
            val event = BeliefAddEvent(beliefName, value)
            events.send(event)
        }
    }

    private fun addBelief(beliefName: String, value: Any?) : Boolean {
        if(beliefs.contains(beliefName)){
            if(beliefs[beliefName] == value){
                say("Belief $beliefName already exists with value $value, ignoring.")
                return false
            }
        }
        beliefs[beliefName] = value
        return true
    }

    /**
     * Drops an intention, killing all the coroutines children of the original intention.
     */
    fun dropIntention(intention: Intention) {
        intentions.find { it.intention == intention }?.let {
            intentions.remove(it)
            it.job.cancel() // Cancel the job associated with the intention
            say("Dropped intention: $intention")
        }
    }

    suspend fun init() {
        initialGoals.forEach{events.send(it)}
    }


    suspend fun step(scope: CoroutineScope) {
        stepNumber++
        val event = events.receive()
        //say("Handling event: $event")
        scope.handleEvent(event)
    }

    /**
     * Runs the agent in a supervisor scope,
     * allowing it to handle failures of child coroutines without affecting the parent coroutine.
     */
    //TODO keeping this to avoid changing everything else, but the best way to run the agent is to
    // use the init() and step() methods deciding externally how to schedule everything
    suspend fun run() = supervisorScope {
        //Initialize the agent
        init()

        //Run the loop
        while(true){
            step(this)
        }
    }

    /**
     * Handles an event by checking its type and executing the corresponding logic.
     */
    private fun CoroutineScope.handleEvent(event: Event) {
        when(event) {
            is AchieveEvent<*> -> { handleAchieveEvent(event)}
            is BeliefAddEvent<*> -> { handleBeliefAddEvent(event) }
            is StepEvent -> { stepIntention() }
            else -> {
                //TODO remove in final version
                log("Unknown event type: $event")
            }
        }
    }

    private fun <T> CoroutineScope.handleAchieveEvent(event: AchieveEvent<T>) {
        val intentionContext = event.intention?.let {
            //If the referenced intention exists, use its context
            intentions.find { it.intention == event.intention } ?: run {
                //If the referenced intention does not exist, create a new one with that ID
                //This is useful for debugging purposes, as it allows to name intentions
                IntentionContext(it, Job(coroutineContext.job))
            }
        } ?: run {
            IntentionContext(Intention(), Job(coroutineContext.job))
        }

        if(!intentions.contains(intentionContext)) {
            intentions.add(intentionContext)
        }

        plans.find { it.trigger == event.planTrigger }?.let {
            launchPlan(it,
                intentionContext,
                PlanContext(event.args, event.completion as CompletableDeferred<Any?>) // TODO Brutto cast
            )
        } ?: run {
            //say("No plan found for event: ${event.planTrigger}")
            // TODO this now completely breaks the agent, but it should not...
            event.completion.completeExceptionally(
                IllegalStateException("No plan found for event: ${event.planTrigger}")
            )
        }
    }

    private fun CoroutineScope.handleBeliefAddEvent(event: BeliefAddEvent<*>) {
        val intentionContext = IntentionContext(Intention(), Job(coroutineContext.job))
        intentions.add(intentionContext)
        //TODO if a belief is added manually, this is done twice (but ignored)
        addBelief(event.beliefName, event.value)
        plans.find { it.trigger == "+${event.beliefName}"}?.let{
            launchPlan(it, intentionContext, PlanContext(listOf(event.value)))
        }
    }

    /**
     * Executes one step of the first available intention.
     */
    private fun stepIntention() {
        //TODO it should not run an intention that has been cancelled
        //TODO why not "receive" and make it suspend??
        // CAREFUL It should NEVER block... this should be called ONLY IF a coroutine is available to continue
        continuations.tryReceive().getOrNull()?.let{
            it()
        }
    }

    /**
     * Launches a plan in a new coroutine within the agent context, the intention context, and its own plan context.
     */
    private fun CoroutineScope.launchPlan(plan: Plan<Any?>, intentionContext: IntentionContext, planContext: PlanContext<Any?>) {
        launch(agentContext + intentionContext + planContext + intentionContext.job) {
            try{
                val result = plan(this)
                planContext.completion.complete(result) // Don't forget to complete the deferred once the plan is done!
            } catch (e: CancellationException) {
                //TODO if we fix the step, this should never happen..
                say("OK, the intention was dropped while ${plan.trigger} was suspended in ")
            }
            catch (e: Exception) {
                say("ERROR in ${plan.trigger}: ${e.message}")
                handleFailedPlan(planContext.completion)
            }
        }
    }

    private suspend fun handleFailedPlan(completion: CompletableDeferred<Any?>) {
        //TODO use a real recovery strategy
        val result = achieve<Unit>("recover")
        completion.complete(result)
    }

}

class AgentContext(val agent: Agent) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key
    companion object Key : CoroutineContext.Key<AgentContext>
}