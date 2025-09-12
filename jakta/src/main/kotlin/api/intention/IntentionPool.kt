package api.intention

interface IntentionPool {
    suspend fun nextIntention(): Intention?
}

interface AddableIntentionPool : IntentionPool {
    fun tryPut(intention: Intention) : Boolean
}

interface MutableIntentionPool : AddableIntentionPool {
    suspend fun drop(intentionID: IntentionID) : Boolean
}
