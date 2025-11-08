package it.unibo.jakta.agent

import it.unibo.jakta.JaktaDSL

@JaktaDSL
interface BeliefBuilder<Belief : Any> {
    operator fun Belief.unaryPlus()
}

class BeliefBuilderImpl<Belief : Any>(val addBelief: (Belief) -> Unit) : BeliefBuilder<Belief> {
    override operator fun Belief.unaryPlus() {
        addBelief(this)
    }
}
