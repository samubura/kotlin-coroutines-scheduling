package api.plan

import api.event.Event
import api.query.BeliefQueryEngine
import api.query.GoalQueryEngine
import api.query.Query

sealed interface PlanTrigger {

    sealed interface Goal<Belief : Any, Query : Any, Result> : PlanTrigger {

        sealed interface Test<TQ: Query.Test> : PlanTrigger {
            val query: TQ

//            fun relevantFor(event: Event.Internal): Boolean =
//                event is Event.Internal.Goal.Test<*, *> &&
//                        event.query == query

            interface Add<TQ: Query.Test> : Test<TQ>

            interface Remove<TQ: Query.Test> : Test<TQ>
        }

        sealed interface Achieve<GoalType : Any, GQ : Query.Goal> : PlanTrigger {
            val query: GQ

//            fun relevantFor(event: Event.Internal, engine: GoalQueryEngine<GoalType, GQ>): Boolean =
//                event is Event.Internal.Goal.Achieve<*, *>
//                        && engine.invoke(query, event.goal as GoalType) != null //TODO check cast

            interface Add<GoalType : Any, GQ : Query.Goal> : Achieve<GoalType, GQ>

            interface Remove<GoalType : Any, GQ : Query.Goal> : Achieve<GoalType, GQ>
        }
    }

    sealed interface Belief<B : Any, BQ : Query.Belief> : PlanTrigger {
        val query: BQ

//        fun relevantFor(event: Event.Internal, engine: BeliefQueryEngine<B, BQ>): Boolean =
//            event is Event.Internal.Belief<*> && engine.invoke(query, event.belief as B) != null //TODO check cast

        interface Add<B : Any, BQ : Query.Belief> : Belief<B, BQ>

        interface Remove<B : Any, BQ : Query.Belief> : Belief<B, BQ>
    }

    //TODO What about external Events?
}
