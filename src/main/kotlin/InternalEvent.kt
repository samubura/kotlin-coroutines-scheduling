import kotlinx.coroutines.Deferred

data class InternalEvent(val planTrigger: String,
                          val completion: Deferred<Unit>)

