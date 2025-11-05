package examples

import api.environment.TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import dsl.mas
import dsl.plan.triggers
import executeInTestScope
import ifGoalMatch
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import time

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
                            assert(System.currentTimeMillis() - time <= 100)
                            agent.print("Time perceived by the agent: ${agent.time()}")
                            assert(agent.time() == timeToWait)
                            agent.print("...World!")
                            agent.terminate()
                        }
                    }
                }
            }
        }
    }
})
