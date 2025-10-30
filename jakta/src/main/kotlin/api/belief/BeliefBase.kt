package api.belief

import BeliefBaseImpl
import api.event.Event
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow

//TODO support more complex Belief bases that have e.g. production rules for inference.
// How would one customize this component?? Right now it is "hidden" inside the AgentImpl..

interface BeliefBase<Belief : Any>
    : MutableCollection<Belief> {
    fun snapshot(): Collection<Belief>

    companion object {
        fun <Belief : Any> empty(agentEvents: SendChannel<Event.Internal.Belief<Belief>>): BeliefBase<Belief> = BeliefBaseImpl(agentEvents)

        fun <Belief : Any> of(agentEvents: SendChannel<Event.Internal.Belief<Belief>>, beliefs: Iterable<Belief>): BeliefBase<Belief> =
            BeliefBaseImpl(agentEvents, beliefs)
    }
}
