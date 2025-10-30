package examples

import api.environment.TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import dsl.mas
import dsl.plan.triggers
import ifGoalMatch
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestBeliefRemoval {
    val helloWorld =
        mas {
            environment { TestEnvironment() }
            agent {
                believes {
                    +"testBelief"
                }
                hasInitialGoals {
                    ! "removeBelief"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("removeBelief")
                    } triggers {
                        agent.forget("testBelief")
                    }
                    removing.belief{
                        this.takeIf{ it == "testBelief" }
                    } triggers {
                        agent.print("Belief removed: $context")
                        agent.terminate()
                    }
                }
            }
        }

    @BeforeEach
    fun setup() {
        Logger.setMinSeverity(Severity.Warn)
    }

    @Test
    fun testBelief() {
        runTest {
            val job =
                launch {
                    helloWorld.run()
                }
            job.join()
        }
    }
}
