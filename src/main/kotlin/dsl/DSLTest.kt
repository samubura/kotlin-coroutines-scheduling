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
                    listOf(TestContext())
                } onlyWhen {
                    listOf(TestContext())
                } triggers {
                    agent.beliefs.contains("???")
                    //TODO Compiler exploding if I remove the next line
                    // Wtf??
                    val x: List<TestContext> = context
                    context.firstOrNull()
                }

                adding.goal {
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