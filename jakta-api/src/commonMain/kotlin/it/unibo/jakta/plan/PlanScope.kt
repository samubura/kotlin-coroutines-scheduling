package it.unibo.jakta.plan

import it.unibo.jakta.agent.AgentActions
import it.unibo.jakta.environment.Environment

interface PlanScope<Belief : Any, Goal : Any, Env : it.unibo.jakta.environment.Environment, Context : Any> {
    val agent: it.unibo.jakta.agent.AgentActions<Belief, Goal>
    val environment: Env
    val context: Context
}


data class PlanScopeImpl<Belief : Any, Goal : Any, Env : it.unibo.jakta.environment.Environment, Context : Any>(
    override val agent: it.unibo.jakta.agent.AgentActions<Belief, Goal>,
    override val environment: Env,
    override val context: Context,
) : it.unibo.jakta.plan.PlanScope<Belief, Goal, Env, Context>
