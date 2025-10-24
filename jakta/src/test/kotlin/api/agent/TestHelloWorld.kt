package api.agent

import examples.TestEnvironment
import dsl.mas
import dsl.plan.triggers
import kotlin.test.Test
import kotlin.test.assertTrue

class TestHelloWorld {

    @Test
    fun testExecuteAchievementGoal() {
        mas {
            environment { TestEnvironment() }
            agent {
                hasInitialGoals {
                    !"executeAchievementGoal"
                }
                hasPlans {
                    adding.goal {
                        "executeAchievementGoal"
                    } triggers {
                        assertTrue(true)
                    }
                }
            }
        }.run()
        //TODO(Check Coroutine book for coroutine testing framework)

    }
}