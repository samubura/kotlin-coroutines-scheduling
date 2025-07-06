import kotlinx.coroutines.CompletableDeferred
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
data class InternalEvent<T>(
    val planTrigger: String,
    val completion: CompletableDeferred<T> = CompletableDeferred<T>(),
    private val intention : String?
) {
    val intentionId : String = intention ?: Uuid.random().toString()
}