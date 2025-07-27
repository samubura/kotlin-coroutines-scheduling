package agent

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

interface Environment { }

class EnvironmentContext(val environment: Environment) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key
    companion object Key : CoroutineContext.Key<EnvironmentContext>
}

class TestEnvironment(private val agents : List<Agent>) : Environment {

    var value = 42
    val mutex: Mutex = Mutex()

    suspend fun sendPerceptions() {
        for (agent in agents) {
            agent.events.send(BeliefAddEvent("x", value))
        }
    }

    suspend fun setValue(newValue: Int){
        mutex.withLock {
            value = newValue;
            sendPerceptions()
        }
    }

}