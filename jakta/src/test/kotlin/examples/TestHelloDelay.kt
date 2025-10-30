package examples

import api.environment.TestEnvironment
import api.intention.IntentionInterceptor
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import ifGoalMatch
import dsl.mas
import dsl.plan.triggers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestHelloDelay {

    val helloWorld =  mas {
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
                    delay(3000) //TODO the delay is still not working properly in tests
                    agent.print("...World!")
                    agent.terminate()
                }
            }
        }
    }

    @BeforeEach
    fun setup(){
        Logger.setMinSeverity(Severity.Debug)
    }

    @Test
    fun testHello(){
        runTest {
            val job = launch {
                helloWorld.run()
            }
            job.join()
        }
    }

}