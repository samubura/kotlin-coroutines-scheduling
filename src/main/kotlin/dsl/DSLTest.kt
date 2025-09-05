package dsl


class TestEnvironment : Environment {
    fun test(): Unit {}
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
                    Regex("pluto").matchEntire(this)
                } onlyWhen {
                    listOfNotNull(it).takeIf { "pippo" in beliefs }
                } triggers {
                    agent.beliefs.contains("???")
                    context.all { it.groups.isNotEmpty() }
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