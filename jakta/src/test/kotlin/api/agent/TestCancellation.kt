package api.agent

import api.environment.EnvironmentContext
import api.environment.TestEnvironment
import api.mas.testingMas
import dsl.agent
import dsl.plan.triggers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.ranges.step
import kotlin.test.Test
import kotlin.test.assertEquals

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
                job.cancel()

            }
        }

    }

    @Test
    fun testlaunchWhile() {
        runBlocking {
            val job = launch {
                while(true) {
                    launch(Job()){
                        delay(1000)
                        println("pipopo")
                    }
                    delay(100)
                }
            }

            launch {
                delay(5000)
                println("DONE")
                job.cancel()
            }
        }
    }

    @Test
    fun tpippo() {
        while(true) {
            println("pippo")
        }
    }
}