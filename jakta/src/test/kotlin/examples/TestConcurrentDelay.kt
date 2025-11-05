package examples

import api.environment.TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import dsl.mas
import dsl.plan.triggers
import executeInTestScope
import ifGoalMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.coroutines.coroutineContext
import kotlin.test.assertEquals

class TestConcurrentDelay {
    val helloWorld =
        mas {
            environment { TestEnvironment() }
            agent {
                hasInitialGoals {
                    !"goal"
                    !"anotherGoal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("Hello...")
                        delay(10000)
                        agent.print("...World!")
                        agent.terminate()
                    }
                    adding.goal {
                        ifGoalMatch("anotherGoal")
                    } triggers {
                        delay(1000)
                        agent.print("Running while waiting...")
                        delay(5000)
                        agent.print("I'm still faster!")
                    }
                }
            }
        }

    @BeforeEach
    fun setup() {
        Logger.setMinSeverity(Severity.Error)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testHello() {
        executeInTestScope { helloWorld }
    }
}
