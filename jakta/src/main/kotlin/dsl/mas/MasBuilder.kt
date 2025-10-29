package dsl.mas

import api.agent.Agent
import api.environment.Environment
import api.mas.MAS
import api.mas.MASImpl
import dsl.JaktaDSL
import dsl.agent.AgentBuilder
import dsl.agent.AgentBuilderImpl


@JaktaDSL
interface MasBuilder<Belief : Any, Goal : Any, Env : Environment> {

    @JaktaDSL
    fun agent(block: AgentBuilder<Belief, Goal, Env>.() -> Unit): Agent<Belief, Goal, Env>

    fun withAgents(vararg agents: Agent<Belief, Goal, Env>)

    fun environment (block: () -> Env)

    fun build() : MAS<Belief, Goal, Env>
}


open class MasBuilderImpl<Belief : Any, Goal : Any, Env : Environment> :
    MasBuilder<Belief, Goal, Env> {

    protected var environment: Env? = null
    protected val agents = mutableListOf<Agent<Belief, Goal, Env>>()

    override fun agent(
        block: AgentBuilder<Belief, Goal, Env>.() -> Unit
    ): Agent<Belief, Goal, Env> {
        val agentBuilder = AgentBuilderImpl<Belief, Goal, Env>()
        val agent = agentBuilder.apply(block).build()
        agents += agent
        return agent
    }

    override fun withAgents(vararg agents: Agent<Belief, Goal, Env>) {
        this.agents += agents
    }

    override fun environment(block: () -> Env) {
        environment = block()
    }

    override fun build(): MAS<Belief, Goal, Env> {
        val env = environment ?: throw IllegalStateException("Must provide an Environment for the MAS")
        return MASImpl(env, agents.toSet())
    }
}