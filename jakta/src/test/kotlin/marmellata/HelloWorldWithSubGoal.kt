package marmellata

import api.agent.achieve
import api.environment.TestEnvironment
import dsl.mas
import dsl.plan.triggers
import ifGoalMatch

suspend fun main() {
    mas {
        environment { TestEnvironment() }
        agent {
            hasInitialGoals {
                !"testAchieve"
            }
            hasPlans {
                adding.goal {
                    ifGoalMatch("testAchieve")
                } triggers {
                    agent.print("Hello World Before!")
                    var p: Int = agent.achieve("subGoal1")
                    agent.print("Hello World After! $p")
                    agent.terminate()
                }

                adding.goal {
                    ifGoalMatch("subGoal1")
                } triggers {
                    agent.print("SubGoal!")
                    42
                }
            }
        }
    }.run()
}
