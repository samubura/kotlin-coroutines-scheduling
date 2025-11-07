package marmellata

import api.environment.TestEnvironment
import dsl.mas
import dsl.plan.triggers
import ifGoalMatch
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest


class TestWithRunTest {

    fun testExecuteAchievementGoal() {
        val mas =
            mas {
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

                            println("muoio")
                            agent.terminate()
                        }
                    }
                }
            }

        runTest { mas.run() }
    }
}
