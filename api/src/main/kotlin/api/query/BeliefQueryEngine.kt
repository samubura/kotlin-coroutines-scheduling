package api.query

interface BeliefQueryEngine<Belief : Any, Q: Query.Belief> : (Q, Belief) -> Belief? {
    abstract override fun invoke(query: Q, belief: Belief): Belief?
}