package api.belief

import api.event.Event
import api.intention.Intention
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal data class BeliefBaseImpl<Belief: Any>(
    val beliefs: MutableSet<Belief> = mutableSetOf(),
    val events : MutableSharedFlow<Event.Internal.Belief<Belief>> = MutableSharedFlow(), //TODO overflow?
) : MutableBeliefBase<Belief>,
    MutableSet<Belief> by beliefs,
    Flow<Event.Internal.Belief<Belief>> by events {

    override fun snapshot(): BeliefBase<Belief> {
        return this.copy()
    }

    override fun add(element: Belief): Boolean = beliefs.add(element).alsoWhenTrue {
        events.tryEmit(element.addEvent())
    }

    override fun remove(element: Belief): Boolean = beliefs.remove(element).alsoWhenTrue {
        events.tryEmit(element.removeEvent())
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
        beliefs.map { it.removeEvent() }
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

        fun <Belief : Any> Belief.removeEvent() = object : Event.Internal.Belief.Remove<Belief> {
            override val belief: Belief get() = this@removeEvent
            override val intention: Intention? = null
        }

        fun <Belief : Any> Belief.addEvent() = object : Event.Internal.Belief.Add<Belief> {
            override val belief: Belief get() = this@addEvent
            override val intention : Intention? = null
        }
    }
}