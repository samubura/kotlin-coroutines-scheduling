package it.unibo.jakta.intention

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class IntentionID(val id: String = _root_ide_package_.it.unibo.jakta.intention.IntentionID.Companion.generateId()) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        private fun generateId(): String = Uuid.random().toString()
    }
}
