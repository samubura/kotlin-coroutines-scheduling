package api.mas

import api.agent.Agent
import api.environment.Environment
import api.environment.EnvironmentContext
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

data class MASImpl<Belief : Any, Goal : Any, Env : Environment>(
    override val environment: Env,
    override val agents: Set<Agent<Belief, Goal, Env>>,
) : MAS<Belief, Goal, Env> {
    private val log =
        Logger(
            Logger.config,
            "MAS",
        )

    override suspend fun run() =
        supervisorScope {
            val environmentContext = EnvironmentContext(environment)
            agents
                .map { agent ->
                    log.d { "Launching agent ${agent.id.id}" }
                    launch(environmentContext) {
                        supervisorScope {
                            log.d { "Agent ${agent.id.id} started" }
                            while (true) {
                                log.d { "Running one step of Agent ${agent.id.id}" }
                                agent.step(this)
                            }
                        }
                    }
                }.joinAll()
        }
}
