package it.unibo.jakta.mas

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.environment.EnvironmentContext
import co.touchlab.kermit.Logger
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

data class MASImpl<Belief : Any, Goal : Any, Env : it.unibo.jakta.environment.Environment>(
    override val environment: Env,
    override val agents: Set<it.unibo.jakta.agent.Agent<Belief, Goal, Env>>,
) : it.unibo.jakta.mas.MAS<Belief, Goal, Env> {
    private val log =
        Logger(
            Logger.config,
            "MAS",
        )

    override suspend fun run() = supervisorScope {
        val environmentContext = _root_ide_package_.it.unibo.jakta.environment.EnvironmentContext(environment)
        agents
            .map { agent ->
                log.d { "Launching agent ${agent.name}" }
                launch(environmentContext) {
                    supervisorScope {
                        log.d { "Agent ${agent.name} started" }
                        while (true) {
                            log.d { "Running one step of Agent ${agent.name}" }
                            agent.step(this)
                        }
                    }
                }
            }.joinAll()
    }
}
