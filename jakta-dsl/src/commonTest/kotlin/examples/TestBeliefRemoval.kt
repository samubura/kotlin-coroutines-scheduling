package examples

import TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import io.kotest.core.spec.style.ShouldSpec
import it.unibo.jakta.mas
import it.unibo.jakta.plan.triggers


class TestBeliefRemoval : ShouldSpec( {
    val helloWorld =
        mas {
            environment { TestEnvironment() }
            agent {
                believes {
                    +"testBelief"
                }
                hasInitialGoals {
                    !"removeBelief"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("removeBelief")
                    } triggers {
                        agent.forget("testBelief")
                    }
                    removing.belief {
                        this.takeIf { it == "testBelief" }
                    } triggers {
                        agent.print("Belief removed: $context")
                        agent.terminate()
                    }
                }
            }
        }

    beforeEach {
        Logger.setMinSeverity(Severity.Warn)
    }

   should("have the belief removal plan triggered") {
        executeInTestScope { helloWorld }
    }
})
