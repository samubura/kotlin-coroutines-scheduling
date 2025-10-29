package api.mas

import api.agent.Agent
import api.environment.Environment
import kotlinx.coroutines.CoroutineScope


interface MAS<Belief : Any, Goal : Any, Env : Environment>{
    val environment: Env
    val agents : Set<Agent<Belief, Goal, Env>>

    suspend fun run() : Unit
}