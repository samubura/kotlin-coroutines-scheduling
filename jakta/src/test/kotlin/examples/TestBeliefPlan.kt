package examples

import api.environment.TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import dsl.mas
import dsl.plan.triggers
import executeInTestScope
import ifGoalMatch
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestBeliefPlan {
    val helloWorld =
        mas {
            environment { TestEnvironment() }
            agent("TestBeliefAgent") {
                believes {
                    +"testBelief"
                }
                hasPlans {
                    adding.belief {
                        this.takeIf { it == "testBelief" }
                    } triggers {
                        agent.print("Belief added: $context")
                        agent.terminate()
                    }
                }
            }
        }

    @BeforeEach
    fun setup() {
        Logger.setMinSeverity(Severity.Error)
    }

    @Test
    fun testBelief() {
        executeInTestScope { helloWorld }
    }
}
