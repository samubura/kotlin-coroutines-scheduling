package examples

import api.environment.TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import dsl.mas
import dsl.plan.triggers
import executeInTestScope
import ifGoalMatch
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay

class TestHelloDelay: ShouldSpec({

    context("An agent that prints hello world with delay in TestScope must not stop, " +
        "but still consider time as elapsed") {
        Logger.setMinSeverity(Severity.Debug)
        val timeToWait = 10000L

        executeInTestScope {
            mas {
                environment { TestEnvironment() }
                agent {
                    hasInitialGoals {
                        !"goal"
                    }
                    hasPlans {
                        adding.goal {
                            ifGoalMatch("goal")
                        } triggers {
                            agent.print("Hello...")
                            val time = System.currentTimeMillis()
                            delay(timeToWait)
                            System.currentTimeMillis() - time shouldBeLessThanOrEqual 100
                            agent.print("Time perceived by the agent: ${environment.currentTime()}")
                            environment.currentTime() shouldBe timeToWait
                            agent.print("...World!")
                            agent.terminate()
                        }
                    }
                }
            }
        }
    }
})
