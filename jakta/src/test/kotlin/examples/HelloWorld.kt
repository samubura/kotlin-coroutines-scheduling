package examples

import api.agent.achieve
import api.environment.TestEnvironment
import ifGoalMatch
import dsl.mas
import dsl.plan.triggers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class TestHello {

    val helloWorld = mas {
        environment { TestEnvironment() }
        agent {
            hasInitialGoals {
                !"goal"
            }
            hasPlans {
                adding.goal {
                    ifGoalMatch("goal")
                } triggers {
                    agent.print("Hello World!")
                    agent.terminate()
                }
            }
        }
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