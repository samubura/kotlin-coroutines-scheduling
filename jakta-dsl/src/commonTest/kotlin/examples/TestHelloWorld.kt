package examples

import TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import io.kotest.core.spec.style.ShouldSpec
import it.unibo.jakta.mas
import it.unibo.jakta.plan.triggers

class TestHelloWorld: ShouldSpec({

    context("An Hello World Agent") {
        Logger.setMinSeverity(Severity.Debug)

        val helloWorld = mas {
            environment { TestEnvironment() }
            agent("HelloAgent") {
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

        should("terminate") {
            executeInTestScope { helloWorld }
        }
    }
})
