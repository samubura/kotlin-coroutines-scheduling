package api.agent

import api.environment.Environment
import api.plan.Plan


interface Agent<Belief : Any, Goal: Any,  Env : Environment> {
    val id: AgentID
    val beliefs: Collection<Belief>
    val beliefPlans: List<Plan.Belief<Belief, Goal, Env, *, *>>
    val goalPlans : List<Plan.Goal<Belief, Goal, Env, *, *>>

    suspend fun init()
    suspend fun step()
}
