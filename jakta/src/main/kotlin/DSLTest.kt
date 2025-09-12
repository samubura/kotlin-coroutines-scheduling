import api.environment.Environment
import dsl.agent
import dsl.mas
import dsl.plan.triggers // TODO maybe not great that we have to import?


class TestEnvironment : Environment {
    fun test(): Unit {}
}

class TestContext() {
    fun test() : Unit {}
}


fun main() {

    val bob = agent<String, Int, TestEnvironment> { }

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
                    result
                }
            }
        }
    }.run()
}