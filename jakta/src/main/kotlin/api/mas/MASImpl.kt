package api.mas

import api.agent.Agent
import api.environment.Environment
import api.environment.EnvironmentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope


data class MASImpl<Belief : Any, Goal : Any, Env : Environment>(
    override val environment: Env,
    override val agents: Set<Agent<Belief, Goal, Env>>
) : MAS<Belief, Goal, Env> {

    override suspend fun run() =  supervisorScope {
        val environmentContext = EnvironmentContext(environment)
        agents.map { agent ->
            launch(environmentContext) { //TODO(Future refactoring into ExecutionStrategy (using Dispatcher) HERE!)
                supervisorScope { // TODO(Double check SupervisorScope is needed here too)
                    agent.start(this)
                    while(true) {
                        agent.step()
                    }
                }
            }
        }.joinAll()
    }
}