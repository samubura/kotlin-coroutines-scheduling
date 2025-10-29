package marmellata

import api.environment.EnvironmentContext
import api.environment.TestEnvironment
import dsl.agent
import dsl.plan.triggers
import ifGoalMatch
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class TestCancellation {

    @Test
    fun testAgentCancellation() {
             val agent = agent {
                 hasInitialGoals {
                     !"executeAchievementGoal"
                 }
                 hasPlans {
                     adding.goal {
                         ifGoalMatch("executeAchievementGoal")
                     } triggers {
                         agent.alsoAchieve("loop")
                         delay(1000)
                         agent.terminate()
                     }

                     adding.goal {
                         ifGoalMatch("loop")
                     } triggers {
                         println("Executing loop!")
                         delay(100)
                         agent.alsoAchieve("loop")

                     }
                 }
             }

        runBlocking {
            val job = launch(EnvironmentContext(TestEnvironment())) {
                agent.start(this)
                while (true) {
                    agent.step()
                }
            }
            launch {
                delay(1000)
                job.cancelAndJoin()
                assert(job.isCancelled)
            }
        }

    }
}