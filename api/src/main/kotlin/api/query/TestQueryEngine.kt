package api.query

import api.belief.BeliefBase

fun interface TestQueryEngine<Belief: Any,
        TQ: Query.Test,
        BBase: BeliefBase<Belief>,
        QueryResult> : (TQ, BBase) -> QueryResult? {
    abstract override fun invoke(query: TQ, beliefBase: BBase): QueryResult?
}