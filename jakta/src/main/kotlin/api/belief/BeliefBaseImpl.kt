import api.belief.BeliefBase
import api.event.BeliefAddEvent
import api.event.BeliefRemoveEvent
import api.event.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal data class BeliefBaseImpl<Belief: Any>(
    val beliefs: MutableSet<Belief> = mutableSetOf(),
    val events : MutableSharedFlow<Event.Internal.Belief<Belief>> = MutableSharedFlow(), //TODO overflow?
) : BeliefBase<Belief>,
    MutableSet<Belief> by beliefs,
    Flow<Event.Internal.Belief<Belief>> by events {

    override fun snapshot(): Collection<Belief> {
        return this.copy()
    }

    override fun add(element: Belief): Boolean = beliefs.add(element).alsoWhenTrue {
        events.tryEmit(BeliefAddEvent(element))
    }

    override fun remove(element: Belief): Boolean = beliefs.remove(element).alsoWhenTrue {
        events.tryEmit( BeliefRemoveEvent(element))
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
        beliefs.map { BeliefRemoveEvent(it) }
            .forEach { events.tryEmit(it) }
        beliefs.clear()

    }

    companion object {
        private fun Boolean.alsoWhenTrue(body: () -> Unit): Boolean = if (this) {
            body()
            true
        } else {
            false
        }
    }
}