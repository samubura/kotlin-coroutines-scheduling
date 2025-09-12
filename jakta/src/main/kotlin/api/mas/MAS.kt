package api.mas

import api.agent.Agent
import api.environment.Environment


interface MAS<Belief : Any, Goal : Any, Env : Environment>{
    val environment: Env
    val agents : Set<Agent<Belief, Goal, Env>>

    fun run() : Unit
}