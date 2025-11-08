package it.unibo.jakta.agent

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AgentID(private val name: String? = null, private val id: String = _root_ide_package_.it.unibo.jakta.agent.AgentID.Companion.generateId()) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        private fun generateId(): String = Uuid.random().toString()
    }

    val displayName : String get() = name ?: "Agent-$id"

    override fun equals(other: Any?): Boolean {
        return other is it.unibo.jakta.agent.AgentID && other.id == this.id
    }

    override fun hashCode(): Int = id.hashCode()
}
