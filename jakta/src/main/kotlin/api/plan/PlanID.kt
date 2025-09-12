package api.plan

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class PlanID(val id: String = generateId()) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        private fun generateId(): String = Uuid.random().toString()
    }
}