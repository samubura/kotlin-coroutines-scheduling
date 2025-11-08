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

class TestMultipleAgentDelays : ShouldSpec({
    val helloWorld =
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
                        delay(10000)
                        agent.print("...World!")
                        agent.terminate()
                    }
                }
            }
            agent {
                hasInitialGoals {
                    !"goal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("I will be faster...")
                        delay(5000)
                        agent.print("...than you!")
                        agent.terminate()
                    }
                }
            }
        }


    beforeEach {
        Logger.setMinSeverity(Severity.Error)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    should("run multiple agents, skipping delays but keeping order"){
        executeInTestScope { helloWorld }
    }
})
