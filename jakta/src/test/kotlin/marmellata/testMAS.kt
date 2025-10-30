package marmellata

import api.environment.TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import dsl.mas
import dsl.plan.triggers
import ifGoalMatch
import kotlinx.coroutines.delay

suspend fun main() {
    Logger.setMinSeverity(Severity.Error)

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
                    agent.print("Hello PIPPO")
                    agent.alsoAchieve("loop")
                    delay(4000)
                    agent.print("GOODBYE PIPPO")
                    agent.terminate()
                }

                adding.goal {
                    ifGoalMatch("loop")
                } triggers {
                    agent.print("Looping...")
                    delay(5)
                    agent.alsoAchieve("loop")
                }
            }
        }
        agent {
            hasInitialGoals {
                !"goal"
            }
            hasPlans {
                adding.goal {
                    ifGoalMatch("goal")
                } triggers {
                    agent.print("Hello PLUTO!")
                    delay(1000)
                    agent.alsoAchieve("goal")
                }
            }
        }
    }.run()
}