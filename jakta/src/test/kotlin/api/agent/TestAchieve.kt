package api.agent

import api.agent.achieve
import api.environment.TestEnvironment
import dsl.mas
import dsl.plan.triggers
import kotlinx.coroutines.delay

fun main(){
    mas {
        environment { TestEnvironment() }
        agent {
            hasInitialGoals {
                !"testAchieve"
            }
            hasPlans {
                adding.goal {
                    if(this == "testAchieve")
                        "pippo"
                    else null
                } triggers {
                    agent.print("Hello World Before!")
                    var p: Int = agent.achieve("subGoal1")
                    agent.print("Hello World After!")
                }

                adding.goal{
                    if(this == "subGoal1")
                        "pippo"
                    else null
                } triggers {
                    agent.print("SubGoalInvocation!")
                    42
                }
            }
        }
    }.run()
}