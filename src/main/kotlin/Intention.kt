@file:OptIn(ExperimentalUuidApi::class)

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Intention(
    var stack: List<suspend () -> Any?>,
    val id : String = Uuid.random().toString(),
) {
    fun stack(body : suspend () -> Any?){
        stack = stack + body
    }
}
