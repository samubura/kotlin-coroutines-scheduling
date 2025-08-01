package examples.environment

import agent.Agent
import agent.BeliefAddEvent
import environment.Environment
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock


class BasicMapEnvironment(private val agents : List<Agent>) : Environment() {

    var data : MutableMap<String, Any> = mutableMapOf()

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

class BreakingEnvironment(private val agents : List<Agent>) : Environment() {

    suspend fun action() {
        mutex.withLock {
            delay(100)
            throw RuntimeException("Action failed")
        }
    }

}