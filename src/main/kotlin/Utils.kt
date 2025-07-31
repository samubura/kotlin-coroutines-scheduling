import agent.Agent
import agent.AgentContext
import agent.PlanContext
import environment.Environment
import environment.EnvironmentContext
import kotlinx.coroutines.CoroutineScope

fun log(message: String) {
    println("${Thread.currentThread()} - $message")
}

//TODO this is better, but probably the future DSL will need to have a more strict scope i.e. planScope
val CoroutineScope.agent : Agent
    get() = coroutineContext[AgentContext.Key]?.agent ?: error("Not inside an AgentContext!")

inline fun <reified T : Environment>  CoroutineScope.environment() : T {
    val env = coroutineContext[EnvironmentContext]?.environment ?: error("Not inside an EnvironmentContext!")
    return env as? T ?: error("Environment is not of type ${T::class.simpleName}, but ${env::class.simpleName}")
}

val CoroutineScope.args : List<Any?>
    get() = coroutineContext[PlanContext.Key]?.args ?: emptyList()

