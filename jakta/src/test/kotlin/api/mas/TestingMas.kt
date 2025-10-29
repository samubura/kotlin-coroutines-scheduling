package api.mas

import api.agent.Agent
import api.environment.Environment
import api.environment.EnvironmentContext
import dsl.mas.MasBuilder
import dsl.mas.MasBuilderImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.collections.forEach

data class TestingMas<Belief : Any, Goal : Any, Env : Environment>(
    override val environment: Env,
    override val agents: Set<Agent<Belief, Goal, Env>>
) : MAS<Belief, Goal, Env> {

    override fun run() {
        //TODO (mock implementation)
        val environmentContext = EnvironmentContext(environment)
        runTest {
            supervisorScope {
                agents.forEach { agent ->
                    launch(environmentContext) { //TODO(Future refactoring into ExecutionStrategy (using Dispatcher) HERE!)
                        supervisorScope { // TODO(Double check SupervisorScope is needed here too)
                            agent.start(this)
                            while(true) {
                                agent.step()
                                println(currentTime)
                            }
                        }
                    }
                }
            }
        }
    }
}

class CustomMasBuilder<Belief: Any , Goal : Any, Env : Environment>(): MasBuilderImpl<Belief, Goal, Env>() {
    override fun build(): MAS<Belief, Goal, Env> {
        val env = environment ?: throw IllegalStateException("Must provide an Environment for the MAS")
        return TestingMas(env, agents.toSet())
    }
}

fun <Belief : Any, Goal : Any, Env : Environment> testingMas(
    block: MasBuilder<Belief, Goal, Env>.() -> Unit
): MAS<Belief, Goal, Env> {
    val mb = CustomMasBuilder<Belief, Goal, Env>()
    mb.apply(block)
    return mb.build()
}