package examples

import TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import io.kotest.core.spec.style.ShouldSpec
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.mas
import it.unibo.jakta.plan.triggers

class TestPlanFailure: ShouldSpec({
    context("A failure in the plan execution") {
        Logger.setMinSeverity(Severity.Warn)

        should("trigger the plan for managing the failure") {
            executeInTestScope {
                mas {
                    environment { TestEnvironment() }
                    agent {
                        hasInitialGoals { !"goalChain" }
                        hasPlans {
                            adding.goal{
                                ifGoalMatch("goalChain")
                            } triggers {
                                val x: Unit = agent.achieve("failingPlan")
                                agent.print("The plan has failed but recovered")
                                agent.terminate()
                            }
                            adding.goal {
                                ifGoalMatch("failingPlan")
                            } triggers {
                                throw Exception("This plan is meant to fail")
                                42
                            }
                            failing.goal {
                                ifGoalMatch("goalChain")
                            } triggers {
                                agent.print("Goal chain failed as expected.")
                                agent.terminate()
                            }
//                            failing.goal {
//                                ifGoalMatch("failingPlan")
//                            } triggers {
//                                agent.print("Plan failed as expected.")
//                            }

                        }
                    }
                }
            }
        }
    }
})
