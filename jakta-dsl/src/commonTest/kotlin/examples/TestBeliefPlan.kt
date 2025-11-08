package examples

import TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import io.kotest.core.spec.style.ShouldSpec
import it.unibo.jakta.mas
import it.unibo.jakta.plan.triggers


class TestBeliefPlan : ShouldSpec({
    val helloWorld =
        mas {
            environment { TestEnvironment() }
            agent("TestBeliefAgent") {
                believes {
                    +"testBelief"
                }
                hasPlans {
                    adding.belief {
                        this.takeIf { it == "testBelief" }
                    } triggers {
                        agent.print("Belief added: $context")
                        agent.terminate()
                    }
                }
            }
        }

    beforeEach {
        Logger.setMinSeverity(Severity.Error)
    }

    should("have the belief plan triggered") {
        executeInTestScope { helloWorld }
    }
})
