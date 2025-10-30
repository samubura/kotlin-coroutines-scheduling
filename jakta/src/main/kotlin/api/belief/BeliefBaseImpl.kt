import api.belief.BeliefBase
import api.event.BeliefAddEvent
import api.event.BeliefRemoveEvent
import api.event.Event
import kotlinx.coroutines.channels.SendChannel

internal data class BeliefBaseImpl<Belief : Any>(
    private val events: SendChannel<Event.Internal.Belief<Belief>>,
    val initialBeliefs: Iterable<Belief> = emptyList(),
) : BeliefBase<Belief>,
    MutableSet<Belief> by mutableSetOf() {
    private val beliefs
        get() = this as MutableSet<Belief>

    init {
        initialBeliefs.forEach { add(it) } // ✅ triggers event
    }

    override fun snapshot(): Collection<Belief> = this.copy()

    override fun add(element: Belief): Boolean =
        beliefs.add(element).alsoWhenTrue {
            events.trySend(BeliefAddEvent(element))
        }

    override fun remove(element: Belief): Boolean =
        beliefs.remove(element).alsoWhenTrue {
            events.trySend(BeliefRemoveEvent(element))
        }

    override fun addAll(elements: Collection<Belief>): Boolean {
        var result = false
        for (belief in elements) {
            result = add(belief) || result
        }
        return result
    }

    override fun removeAll(elements: Collection<Belief>): Boolean {
        var result = false
        for (belief in elements) {
            result = remove(belief) || result
        }
        return result
    }

    override fun retainAll(elements: Collection<Belief>): Boolean {
        var result = false
        for (belief in beliefs) {
            if (belief !in elements) {
                result = remove(belief) || result
            }
        }
        return result
    }

    override fun clear() {
        beliefs
            .map { BeliefRemoveEvent(it) }
            .forEach { events.trySend(it) }
        beliefs.clear()
    }

    companion object {
        private fun Boolean.alsoWhenTrue(body: () -> Unit): Boolean =
            if (this) {
                body()
                true
            } else {
                false
            }
    }
}
