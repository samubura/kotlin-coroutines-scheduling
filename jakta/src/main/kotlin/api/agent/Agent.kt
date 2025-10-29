package api.agent

import api.environment.Environment
import api.plan.Plan
import kotlinx.coroutines.CoroutineScope


interface Agent<Belief : Any, Goal: Any,  Env : Environment> {
    val id: AgentID
    val beliefs: Collection<Belief>
    val beliefPlans: List<Plan.Belief<Belief, Goal, Env, *, *>>
    val goalPlans : List<Plan.Goal<Belief, Goal, Env, *, *>>

    fun start(scope: CoroutineScope)
    suspend fun stop()
    suspend fun step()
}
