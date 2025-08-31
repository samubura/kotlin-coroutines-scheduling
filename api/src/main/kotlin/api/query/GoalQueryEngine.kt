package api.query

interface GoalQueryEngine<Goal, Q: Query.Goal> : (Q, Goal) -> Goal? {
    abstract override fun invoke(query: Q, goal: Goal): Goal?
}