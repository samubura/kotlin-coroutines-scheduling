package it.unibo.jakta.belief

import it.unibo.jakta.event.Event
import kotlinx.coroutines.channels.SendChannel

// TODO support more complex Belief bases that have e.g. production rules for inference.
// How would one customize this component?? Right now it is "hidden" inside the AgentImpl..

interface BeliefBase<Belief : Any> : MutableCollection<Belief> {
    fun snapshot(): Collection<Belief>

    companion object {
        fun <Belief : Any> empty(agentEvents: SendChannel<it.unibo.jakta.event.Event.Internal.Belief<Belief>>): it.unibo.jakta.belief.BeliefBase<Belief> =
            _root_ide_package_.it.unibo.jakta.belief.BeliefBaseImpl(agentEvents)

        fun <Belief : Any> of(
            agentEvents: SendChannel<it.unibo.jakta.event.Event.Internal.Belief<Belief>>,
            beliefs: Iterable<Belief>,
        ): it.unibo.jakta.belief.BeliefBase<Belief> =
            _root_ide_package_.it.unibo.jakta.belief.BeliefBaseImpl(agentEvents, beliefs)
    }
}
