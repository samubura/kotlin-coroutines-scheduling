package dsl


class TestEnvironment : Environment {
    fun test(): Unit {}
}

class TestContext() {
    fun test() : Unit {}
}


fun main() {
    mas {
        environment {
            TestEnvironment()
        }
        agent {

            believes {
                + "pippo"
                + "pluto"
            }

            hasInitialGoals {
                ! 27
            }

            hasPlans {
                adding.belief {
                    listOf("a")
                } onlyWhen {
                    listOf("b")
                } triggers {
                    context.firstOrNull()
                    true
                }

                failing.goal {
                    takeIf{it >= 27}
                } triggers {
                    val x = context + 10
                    environment.test()
                    val result : String = agent.achieve(23)
                }
            }
        }
    }.run()
}