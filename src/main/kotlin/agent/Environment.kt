package agent

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

interface Environment { }

class EnvironmentContext(val environment: Environment) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key
    companion object Key : CoroutineContext.Key<EnvironmentContext>
}


class BasicMapEnvironment(private val agents : List<Agent>) : Environment {

    var data : MutableMap<String, Any> = mutableMapOf()
    val mutex: Mutex = Mutex()

    suspend fun sendPerceptions(key : String, value : Any) {
        for (agent in agents) {
            agent.events.send(
                BeliefAddEvent(key, value)
            )
        }
    }

    suspend fun set(key: String, value: Any) {
        mutex.withLock {
            data[key] = value
            sendPerceptions(key, value)
        }
    }

}

class BreakingEnvironment(private val agents : List<Agent>) : Environment {
    val mutex: Mutex = Mutex()

    suspend fun action() {
        mutex.withLock {
            delay(100)
            throw RuntimeException("Action failed")
        }
    }

}