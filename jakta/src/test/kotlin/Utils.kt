import api.agent.Agent
import api.agent.AgentActions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime

fun <T> String.ifGoalMatch(goal: String, returnValue: T): T? = if (this == goal) returnValue else null

fun String.ifGoalMatch(goal: String): Unit? = if (this == goal) Unit else null

@OptIn(ExperimentalCoroutinesApi::class)
context(coroutineScope: TestScope)
fun <Belief: Any, Goal: Any> AgentActions<Belief, Goal>.time() : Long = coroutineScope.currentTime
