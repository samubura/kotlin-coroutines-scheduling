package api.query

sealed interface Query {

    interface Belief : Query

    interface Test : Query

    interface Goal : Query
}