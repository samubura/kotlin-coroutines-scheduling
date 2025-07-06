package agent

import Plan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch



class Agent(
    val plans: Map<String, Plan<*>>,
    val selectIntention : (List<Intention>) -> Intention
){
    //TODO set to a proper AgentScope
    private val agentScope: CoroutineScope = CoroutineScope(
        Dispatchers.IO
    )

    val intentions : MutableList<Intention> = mutableListOf()
    val events = Channel<Event>(Channel.Factory.UNLIMITED)

    suspend fun run() {
        val event = events.receive()
        handleEvent(event)
        step()
        if(events.tryReceive().isFailure && intentions.isNotEmpty()){ //proactive behaviour
            events.send(StepEvent())
        }
    }

    private fun handleEvent(event : Event) =
        when(event) {
            is AchieveEvent<*> -> achieve(event)
            is StepEvent -> Unit
        }

    private fun achieve(event: AchieveEvent<*>) {
        plans[event.planTrigger]?.also { plan ->
            val intention = event.intentionID?.let { id ->
                intentions.firstOrNull{ it.id == id }
            }
            intention?.stack(plan.body) ?: intentions.add(Intention(listOf(plan.body)))
        }
    }


    private fun step(){
        val intention = selectIntention(intentions)
        agentScope.launch {
            intention.stack.first().invoke()
        }

    }

}