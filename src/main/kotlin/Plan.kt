data class Plan<T>(val id: String,
                   val body: suspend () -> T)