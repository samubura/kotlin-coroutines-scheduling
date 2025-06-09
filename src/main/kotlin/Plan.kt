import kotlinx.coroutines.CoroutineScope

data class Plan(val id: String,
                val runnable: suspend CoroutineScope.() -> Unit)