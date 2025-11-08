package it.unibo.jakta.agent

import it.unibo.jakta.environment.Environment
import it.unibo.jakta.event.Event
import it.unibo.jakta.plan.Plan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel

interface Agent<Belief : Any, Goal : Any, Env : it.unibo.jakta.environment.Environment> : SendChannel<it.unibo.jakta.event.Event.Internal> {
    val name : String
    val beliefs: Collection<Belief>
    val beliefPlans: List<it.unibo.jakta.plan.Plan.Belief<Belief, Goal, Env, *, *>>
    val goalPlans: List<it.unibo.jakta.plan.Plan.Goal<Belief, Goal, Env, *, *>>

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
