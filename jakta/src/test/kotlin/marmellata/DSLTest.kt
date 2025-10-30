package marmellata

import api.agent.achieve
import api.environment.TestEnvironment
import dsl.BeliefPlan
import dsl.agent
import dsl.mas
import dsl.plan.triggers // TODO maybe not great that we have to import?
import ifGoalMatch

suspend fun main() {
    val p =
        BeliefPlan.of<String, Int, TestEnvironment> {
            removing.belief { listOf(1, 2, 3) } triggers { println("triggered") }
        }
    val bob = agent<String, Int, TestEnvironment> { withBeliefPlans(p) }

    mas {
        environment {
            TestEnvironment()
        }

        withAgents(bob)

        agent {
            believes {
                +"pippo"
                +"pluto"
            }

            hasInitialGoals {
                !27
            }

            hasPlans {
                adding.belief {
                    listOf("a")
                } onlyWhen {
                    listOf("b")
                } triggers {
                    agent.print("Hello")
                    context.firstOrNull()
                    true
                }

                failing.goal {
                    takeIf { it >= 27 }
                } triggers {
                    val x = context + 10
                    environment.test()
                    val result: String = agent.achieve(23)
                    // Filtering plans on the return type of execution. Requires generic type reification.
                    result
                }
            }
        }
    }

    mas {
        environment { TestEnvironment() }
        agent {
            hasInitialGoals {
                !"addKnowledge"
            }
            hasPlans {
                adding.goal {
                    ifGoalMatch("addKnowledge", 2)
                } triggers {
                    agent.print("Hello World!")
                }
            }
        }
    }.run()
}
