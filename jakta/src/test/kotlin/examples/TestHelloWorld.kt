package examples

import api.environment.TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import dsl.mas
import dsl.plan.triggers
import ifGoalMatch
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestHelloWorld: ShouldSpec({

    context("An Hello World Agent") {
        Logger.setMinSeverity(Severity.Debug)

        val helloWorld = mas {
            environment { TestEnvironment() }
            agent("HelloAgent") {
                hasInitialGoals {
                    !"goal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("Hello World!")
                        agent.terminate()
                    }
                }
            }
        }

        should("terminate") {
            runTest {
                val job = launch {
                    helloWorld.run()
                }
                job.join()
            }
        }
    }

})
