package api.mas

import api.agent.Agent
import api.environment.Environment


data class MASImpl<Belief : Any, Goal : Any, Env : Environment>(
    override val environment: Env,
    override val agents: Set<Agent<Belief, Goal, Env>>
) : MAS<Belief, Goal, Env> {

    override fun run() {
        //TODO Implement the real one
        agents.forEach{print(it)}
    }
}