package examples

import api.agent.achieve
import api.environment.TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import ifGoalMatch
import dsl.mas
import dsl.plan.triggers
import kotlinx.coroutines.delay

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