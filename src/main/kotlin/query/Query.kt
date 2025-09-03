package query

sealed interface Query<Result> {

    interface Belief<Belief : Any, Result> : Query<Result>

    interface Goal<Result> : Query<Result>
}