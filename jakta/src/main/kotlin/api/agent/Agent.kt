package api.agent

import api.environment.Environment
import api.event.Event
import api.plan.Plan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel

interface Agent<Belief : Any, Goal : Any, Env : Environment> : SendChannel<Event.Internal> {
    val id: AgentID
    val beliefs: Collection<Belief>
    val beliefPlans: List<Plan.Belief<Belief, Goal, Env, *, *>>
    val goalPlans: List<Plan.Goal<Belief, Goal, Env, *, *>>

    /**
     * Runs a step of a reasoning cycle, suspends until an event is available and process it.
     * Can launch plans in response to the event.
     * @param scope must be a SupervisorScope
     */
    suspend fun step(scope: CoroutineScope)

    /**
     * Stops the agent, cancelling its main Job.
     */
    suspend fun stop()
}
