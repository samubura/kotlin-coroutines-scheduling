package marmellata

import api.environment.TestEnvironment
import dsl.mas
import dsl.plan.triggers
import ifGoalMatch
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestWithRunTest {

    @Test
    fun testExecuteAchievementGoal() {
        val mas = mas {
            environment { TestEnvironment() }
            agent {
                hasInitialGoals {
                    !"executeAchievementGoal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("executeAchievementGoal", "pippo")
                    } triggers {
                        println("inizio")
                        delay(5000)
                        println("assert")
                        assertEquals(this.context, "pippo")
                        println("muoio")
                        agent.terminate()
                    }
                }
            }
        }

        runTest { mas.run() }
    }
}