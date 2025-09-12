package dsl

import api.agent.Agent
import api.environment.Environment
import api.mas.MAS
import dsl.agent.AgentBuilder
import dsl.agent.AgentBuilderImpl
import dsl.mas.MasBuilder
import dsl.mas.MasBuilderImpl


@JaktaDSL
fun <Belief : Any, Goal : Any, Env : Environment> mas(
    block: MasBuilder<Belief, Goal, Env>.() -> Unit
): MAS<Belief, Goal, Env> {
    val mb = MasBuilderImpl<Belief, Goal, Env>()
    mb.apply(block)
    return mb.build()
}

@JaktaDSL
fun <Belief : Any, Goal : Any, Env : Environment> agent(block: AgentBuilder<Belief, Goal, Env>.() -> Unit): Agent<Belief, Goal, Env> {
    val ab = AgentBuilderImpl<Belief, Goal, Env>()
    ab.apply(block)
    return ab.build()
}

//TODO entrypoint for plans???
// this is tricky due to the way the DSL is constructed
// create an entrypoint for a single standalone plan is hard...



