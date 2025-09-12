package api.plan

import api.agent.AgentActions
import api.environment.Environment
import dsl.JaktaDSL

@JaktaDSL
interface PlanScope<Belief : Any, Goal: Any, Env : Environment, Context : Any> {
    val agent: AgentActions<Belief, Goal>
    val environment: Env
    val context : Context
}

@JaktaDSL
data class PlanScopeImpl<Belief : Any, Goal : Any, Env : Environment, Context : Any>(
    override val agent: AgentActions<Belief, Goal>,
    override val environment: Env,
    override val context: Context
) : PlanScope<Belief, Goal, Env, Context>