package api.agent

import api.environment.Environment
import api.plan.GuardScope
import api.plan.Plan



data class AgentImpl<Belief : Any, Goal : Any, Env : Environment>(
    val initialBeliefs: Collection<Belief>,
    val initialGoals : List<Goal>,
    override val beliefPlans: List<Plan.Belief<Belief, Goal, Env, *, *>>,
    override val goalPlans: List<Plan.Goal<Belief, Goal, Env, *, *>>,
    override val id: AgentID = AgentID(), //TODO Check
) : Agent<Belief, Goal, Env>,
    AgentActions<Belief, Goal>,
    GuardScope<Belief>
{
    override val beliefs: Collection<Belief>
        get() = TODO("Not yet implemented")


    override suspend fun init() {
        initialGoals.forEach { alsoAchieve(it) }
    }

    override suspend fun step() {
        TODO("Not yet implemented")
    }

    override suspend fun <PlanResult> achieve(goal: Goal): PlanResult {
        TODO("Not yet implemented")
    }

    override fun print(message: String) {
        TODO("Not yet implemented")
    }

    override fun alsoAchieve(goal: Goal) {
        TODO("Not yet implemented")
    }

    override fun fail() {
        TODO("Not yet implemented")
    }

    override fun succeed() {
        TODO("Not yet implemented")
    }

    override suspend fun believe(belief: Belief) {
        TODO("Not yet implemented")
    }

    override suspend fun forget(belief: Belief) {
        TODO("Not yet implemented")
    }

}
