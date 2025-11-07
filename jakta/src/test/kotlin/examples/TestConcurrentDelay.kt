package examples

import api.environment.TestEnvironment
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.coroutines.ExperimentalCoroutinesApi
import co.touchlab.kermit.*
import dsl.mas
import dsl.plan.triggers
import executeInTestScope
import ifGoalMatch
import kotlinx.coroutines.delay

class TestConcurrentDelaySpec : ShouldSpec({

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

    beforeEach {
        Logger.setMinSeverity(Severity.Error)
    }

    should("run Hello World concurrently") {
        @OptIn(ExperimentalCoroutinesApi::class)
        executeInTestScope { helloWorld }
    }
})
