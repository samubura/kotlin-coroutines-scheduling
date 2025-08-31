package api

import api.agent.Agent
import api.environment.Environment
import api.intention.Intention
import kotlinx.coroutines.CoroutineScope

interface AgentScope : CoroutineScope {
    val agent: Any //TODO
}

interface EnvironmentScope<T : Environment>: CoroutineScope {
    val environment: T
}

interface IntentionScope : AgentScope {
    val intention : Intention
}

interface PlanScope<T: Environment> : IntentionScope, EnvironmentScope<T> {
    val args: List<Any>?
}

