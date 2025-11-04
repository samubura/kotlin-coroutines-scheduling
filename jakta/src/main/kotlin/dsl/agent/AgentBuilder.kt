package dsl.agent

import api.agent.Agent
import api.agent.AgentID
import api.agent.AgentImpl
import api.environment.Environment
import api.plan.Plan
import dsl.JaktaDSL
import dsl.plan.PlanLibraryBuilder
import dsl.plan.PlanLibraryBuilderImpl

@JaktaDSL
interface AgentBuilder<Belief : Any, Goal : Any, Env : Environment> {
    fun believes(block: BeliefBuilder<Belief>.() -> Unit)

    fun hasInitialGoals(block: GoalBuilder<Goal>.() -> Unit)

    fun hasPlans(block: PlanLibraryBuilder<Belief, Goal, Env>.() -> Unit)

    fun addBelief(belief: Belief)

    fun addGoal(goal: Goal)

    fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Env, *, *>)

    fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Env, *, *>)

    fun withBeliefPlans(vararg plans: Plan.Belief<Belief, Goal, Env, *, *>)

    fun withGoalPlans(vararg plans: Plan.Goal<Belief, Goal, Env, *, *>)

    fun build(): Agent<Belief, Goal, Env>
}

class AgentBuilderImpl<Belief : Any, Goal : Any, Env : Environment>(val name: String? = null) : AgentBuilder<Belief, Goal, Env> {
    private var initialBeliefs = listOf<Belief>()
    private var initialGoals = listOf<Goal>()
    private var beliefPlans = listOf<Plan.Belief<Belief, Goal, Env, *, *>>()
    private var goalPlans = listOf<Plan.Goal<Belief, Goal, Env, *, *>>()

    override fun believes(block: BeliefBuilder<Belief>.() -> Unit) {
        val builder = BeliefBuilderImpl(::addBelief)
        builder.apply(block)
    }

    override fun hasInitialGoals(block: GoalBuilder<Goal>.() -> Unit) {
        val builder = GoalBuilderImpl(::addGoal)
        builder.apply(block)
    }

    override fun hasPlans(block: PlanLibraryBuilder<Belief, Goal, Env>.() -> Unit) {
        val builder = PlanLibraryBuilderImpl(::addBeliefPlan, ::addGoalPlan)
        builder.apply(block)
    }

    override fun addBelief(belief: Belief) {
        initialBeliefs += belief
    }

    override fun addGoal(goal: Goal) {
        initialGoals += goal
    }

    override fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Env, *, *>) {
        beliefPlans += plan
    }

    override fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Env, *, *>) {
        goalPlans += plan
    }

    override fun withBeliefPlans(vararg plans: Plan.Belief<Belief, Goal, Env, *, *>) {
        beliefPlans += plans
    }

    override fun withGoalPlans(vararg plans: Plan.Goal<Belief, Goal, Env, *, *>) {
        goalPlans += plans
    }

    override fun build(): Agent<Belief, Goal, Env> =
        AgentImpl(initialBeliefs, initialGoals, beliefPlans, goalPlans, name?.let { AgentID(it) } ?: AgentID())
}
