package api.agent

import api.environment.TestEnvironment
import api.mas.testingMas
import dsl.plan.triggers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.coroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestHelloWorld {

    @Test
    fun testExecuteAchievementGoal() {
        testingMas {
            environment { TestEnvironment() }
            agent {
                hasInitialGoals {
                    !"executeAchievementGoal"
                }
                hasPlans {
                    adding.goal {
                        if(this == "executeAchievementGoal")
                            "pippo"
                        else null
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
        }.run()
    }
}