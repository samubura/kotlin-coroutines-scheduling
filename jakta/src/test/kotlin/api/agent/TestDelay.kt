package api.agent

import api.environment.TestEnvironment
import dsl.mas
import dsl.plan.triggers
import kotlinx.coroutines.delay

fun main(){
    mas {
        environment { TestEnvironment() }
        agent {
            hasInitialGoals {
                !"performDelay"
            }
            hasPlans {
                adding.goal {
                    "performDelay"
                } triggers {
                    agent.print("Hello World Before!")
                    delay(5000)
                    agent.print("Hello World After!")
                }
            }
        }
    }.run()
}