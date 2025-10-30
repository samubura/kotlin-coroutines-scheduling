package examples

import api.environment.TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import dsl.mas
import dsl.plan.triggers
import ifGoalMatch
import kotlinx.coroutines.delay

suspend fun main() {
    Logger.setMinSeverity(Severity.Debug)

    mas {
        environment { TestEnvironment() }
        agent {
            hasInitialGoals {
                !"goal"
            }
            hasPlans {
                adding.goal {
                    ifGoalMatch("hello")
                } triggers {
                    agent.print("Hello World")
                    delay(3000)
                    agent.print("Goodbye World!")
                }
            }
        }
//        agent {
//            hasInitialGoals {
//                !"goal"
//            }
//            hasPlans {
//                adding.goal {
//                    ifGoalMatch("goal")
//                } triggers {
//                    agent.print("Hello PLUTO!")
//                    delay(1000)
//                    agent.alsoAchieve("goal")
//                }
//            }
//        }
    }.run()
}