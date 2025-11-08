package examples

import TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import io.kotest.core.spec.style.ShouldSpec
import it.unibo.jakta.mas
import it.unibo.jakta.plan.triggers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay


class TestConcurrentDelay : ShouldSpec({

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
