package api.belief

interface MutableBeliefBase<Belief : Any> : BeliefBase<Belief>, MutableCollection<Belief> {
    fun snapshot(): BeliefBase<Belief>

    companion object {
        fun <Belief : Any> empty(): MutableBeliefBase<Belief> = BeliefBaseImpl()

        fun <Belief : Any> of(beliefs: Iterable<Belief>): MutableBeliefBase<Belief> =
            BeliefBaseImpl(beliefs.toMutableSet())

        fun <Belief : Any> of(vararg beliefs: Belief): MutableBeliefBase<Belief> = of(beliefs.asList())
    }
}