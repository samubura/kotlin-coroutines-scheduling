package examples

import api.environment.TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import dsl.mas
import dsl.plan.triggers
import ifGoalMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import time

class TestHelloDelay {

    @BeforeEach
    fun setup() {
        Logger.setMinSeverity(Severity.Debug)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testHello() {
        runTest {
            val job =
                launch {
                    mas {
                        environment { TestEnvironment() }
                        agent {
                            hasInitialGoals {
                                !"goal"
                            }
                            hasPlans {
                                adding.goal {
                                    ifGoalMatch("goal")
                                } triggers {
                                    agent.print("Hello...")
                                    delay(10000)
                                    agent.print("Time perceived by the agent: ${agent.time()}")
                                    assert(agent.time() == 10000L)
                                    agent.print("...World!")
                                    agent.terminate()
                                }
                            }
                        }
                    }.run()
                }
            job.join()
        }
    }
}
