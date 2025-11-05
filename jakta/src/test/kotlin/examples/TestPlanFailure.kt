package examples

import api.environment.TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import dsl.mas
import dsl.plan.triggers
import executeInTestScope
import ifGoalMatch
import io.kotest.core.spec.style.ShouldSpec
import org.junit.jupiter.api.assertThrows

class TestPlanFailure: ShouldSpec({
    context("A failure in the plan execution") {
        Logger.setMinSeverity(Severity.Debug)

        should("trigger the plan for managing the failure") {
            assertThrows< InterruptedException> {
                executeInTestScope {
                    mas {
                        environment { TestEnvironment() }
                        agent {
                            hasInitialGoals { !"failingPlan" }
                            hasPlans {
                                adding.goal {
                                    ifGoalMatch("failingPlan")
                                } triggers {
                                    throw InterruptedException()
                                    42
                                }
                            }
                        }
                    }
                }

            }
        }
    }


})
