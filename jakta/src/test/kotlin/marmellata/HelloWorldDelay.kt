package marmellata

import api.environment.TestEnvironment
import dsl.mas
import dsl.plan.triggers
import ifGoalMatch
import kotlinx.coroutines.delay

suspend fun main() {
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
                    delay(1000)
                    agent.print("...World!")
                    agent.terminate()
                }
            }
        }
    }.run()
}
