package examples

import api.environment.EnvironmentContext
import api.environment.TestEnvironment
import dsl.agent
import dsl.plan.triggers
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
                         if (this == "executeAchievementGoal")
                             "pippo"
                         else null
                     } triggers {
                         agent.alsoAchieve("loop")
                         delay(1000)
                         agent.terminate()
                     }

                     adding.goal {
                         if (this == "loop")
                             "pippo"
                         else null
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
                while(true) {
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