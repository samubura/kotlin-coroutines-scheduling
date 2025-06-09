import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

class RoundRobinDispatcher(plans : Sequence<Plan>) : IntentionDispatcher(plans) {
    private val intentionOrder = ArrayDeque<String>()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        val planID = context[PlanContextKey]?.intentionId ?: error("Missing TaskId in context")
        if (!intentions.containsKey(planID)) {
            intentionOrder.addLast(planID)
        }
        super.dispatch(context, block)
    }

    override fun selectNextIntention(): String? {
        val size = intentionOrder.size
        repeat(size) {
            val candidate = intentionOrder.removeFirst()
            intentionOrder.addLast(candidate)

            if (intentions[candidate]?.isNotEmpty() == true
                && !suspendedIntentions.contains(candidate)) {
                return candidate
            }
        }
        return null
    }

    override fun cleanIntentions() {
        super.cleanIntentions()
        intentionOrder.removeIf { id -> !intentions.containsKey(id) }
    }
}