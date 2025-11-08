package it.unibo.jakta.mas

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.environment.Environment

interface MAS<Belief : Any, Goal : Any, Env : it.unibo.jakta.environment.Environment> {
    val environment: Env
    val agents: Set<it.unibo.jakta.agent.Agent<Belief, Goal, Env>>

    suspend fun run(): Unit
}
