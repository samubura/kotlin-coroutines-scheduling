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

class TestHelloWorld {

    val helloWorld = mas {
        environment { TestEnvironment() }
        agent("HelloAgent") {
            hasInitialGoals {
                !"goal"
            }
            hasPlans {
                adding.goal {
                    ifGoalMatch("goal")
                } triggers {
                    agent.print("Hello World!")
                    agent.terminate()
                }
            }
        }
    }

    @BeforeEach
    fun setup() {
        Logger.setMinSeverity(Severity.Debug)
    }

    @Test
    fun testHello() {
        runTest {
            val job = launch {
                helloWorld.run()
            }
            job.join()
        }
    }
}
