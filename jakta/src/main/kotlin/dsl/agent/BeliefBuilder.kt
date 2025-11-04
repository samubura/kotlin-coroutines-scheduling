package dsl.agent

import dsl.JaktaDSL

@JaktaDSL
interface BeliefBuilder<Belief : Any> {
    operator fun Belief.unaryPlus()
}

class BeliefBuilderImpl<Belief : Any>(val addBelief: (Belief) -> Unit) : BeliefBuilder<Belief> {
    override operator fun Belief.unaryPlus() {
        addBelief(this)
    }
}
