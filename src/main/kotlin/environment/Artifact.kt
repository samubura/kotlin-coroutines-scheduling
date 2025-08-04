@file:OptIn(ExperimentalUuidApi::class)

package environment
import agent.Agent
import agent.BeliefAddEvent
import agent.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid



abstract class Artifact {

    private val agents: MutableSet<Agent> = mutableSetOf()

    val mutex: Mutex = Mutex()

    private val properties : MutableMap<String, Any?> = mutableMapOf()

    val id: Uuid
        get() = Uuid.random()

    abstract suspend fun init(scope: CoroutineScope)

    suspend fun focus(agent: Agent) {
        mutex.withLock {
            if (!agents.contains(agent)) {
                agents.add(agent)
            }
        }
    }

    suspend fun unfocus(agent: Agent) {
        mutex.withLock {
            agents.remove(agent)
        }
    }

    protected suspend fun <T> defineProperty(key: String, value: T) {
        if (properties.containsKey(key)) {
            throw IllegalArgumentException("Property $key already defined")
        }
        properties[key] = value
    }

    protected suspend fun <T> getProperty(key: String): T {
        if (!properties.containsKey(key)) {
                throw IllegalArgumentException("Property $key not defined")
            }
        return properties[key] as T
    }

    protected suspend fun <T> updateProperty(key: String, value: T) {
        if (!properties.containsKey(key)) {
            throw IllegalArgumentException("Property $key not defined")
        }
        properties[key] = value
        notifyAgents(BeliefAddEvent(key, value))
    }

    //TODO subtype of event specific for artifacts
    protected suspend fun trigger(event: Event) {
        notifyAgents(event)
    }

    private fun notifyAgents(event: Event) {
        for (agent in agents) {
            agent.events.trySend(event)
        }
    }
}

class ArtifactBasedEnvironment(initialArtifacts: Set<Artifact>) : Environment() {

    private val artifacts : MutableSet<Artifact> = initialArtifacts.toMutableSet()
    private var scope: CoroutineScope? = null

    val artifactSet : Set<Artifact>
        get() = artifacts

    suspend fun init(scope: CoroutineScope) {
        this.scope = scope
        mutex.withLock {
            for (artifact in artifacts) {
                artifact.init(scope)
            }
        }
    }

    suspend fun getArtifactById(id: Uuid): Artifact? {
        mutex.withLock{
            return artifacts.find { it.id == id }
        }
    }

    suspend inline fun <reified T : Artifact> getArtifact(): T? {
        mutex.withLock {
            return artifactSet.firstOrNull { it is T }?.let {it as? T}
        }
    }

    suspend fun addArtifact(artifact: Artifact) {
        mutex.withLock {
            artifacts.add(artifact)
            scope?.launch {
                artifact.init(this)
            }
        }
    }

    suspend fun removeArtifact(artifact: Artifact) {
        mutex.withLock {
            artifacts.remove(artifact)
        }
    }
}