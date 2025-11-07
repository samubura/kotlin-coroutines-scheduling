package marmellata

import api.environment.TestEnvironment
import dsl.mas
import dsl.plan.triggers
import ifGoalMatch
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class TestDelay {

    fun testDelay() = runTest {
        mas {
            environment { TestEnvironment() }
            agent {
                hasInitialGoals {
                    !"performDelay"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("performDelay")
                    } triggers {
                        agent.print("Hello World Before!")
                        delay(5000)
                        agent.print("Hello World After!")
                        agent.terminate()
                    }
                }
            }
        }.run()
    }
}
