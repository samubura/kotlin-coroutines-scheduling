package api.belief

import BeliefBaseImpl
import api.event.Event
import kotlinx.coroutines.flow.Flow

//TODO support more complex Belief bases that have e.g. production rules for inference.
// How would one customize this component?? Right now it is "hidden" inside the AgentImpl..

interface BeliefBase<Belief : Any>
    : MutableCollection<Belief>, Flow<Event.Internal.Belief<Belief>> {
    fun snapshot(): Collection<Belief>

    companion object {
        fun <Belief : Any> empty(): BeliefBase<Belief> = BeliefBaseImpl()

        fun <Belief : Any> of(beliefs: Iterable<Belief>): BeliefBase<Belief> =
            BeliefBaseImpl(beliefs.toMutableSet())
    }
}
