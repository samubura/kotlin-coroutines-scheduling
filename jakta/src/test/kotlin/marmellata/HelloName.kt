package marmellata

import api.environment.TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import dsl.mas
import dsl.plan.triggers

suspend fun main(){

    Logger.setMinSeverity(Severity.Verbose)

    data class goal(val goalID: String, val param: String)

    mas {
        environment { TestEnvironment() }
        agent {
            hasInitialGoals {
                !goal("hello", "Jakta")
            }
            hasPlans {
                adding.goal {
                    this.param.takeIf { this.goalID == "hello" }
                } triggers {
                    agent.print("Hello ${this.context}!")
                    agent.terminate()
                }
            }
        }
    }.run()
}