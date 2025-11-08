package it.unibo.jakta.plan

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class PlanID(val id: String = _root_ide_package_.it.unibo.jakta.plan.PlanID.Companion.generateId()) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        private fun generateId(): String = Uuid.random().toString()
    }
}
