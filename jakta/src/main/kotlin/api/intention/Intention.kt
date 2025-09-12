package api.intention

import kotlinx.coroutines.Job

interface Intention {
    val id : IntentionID
    val continuation: () -> Unit
    val job : Job

    companion object {
        operator fun invoke(
            id: IntentionID,
            job : Job,
            continuation: () -> Unit,
        ): Intention = object : Intention {
            override val id: IntentionID = id
            override val job = job
            override val continuation: () -> Unit = continuation
        }
    }
}
