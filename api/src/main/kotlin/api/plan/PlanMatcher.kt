package api.plan

import api.belief.BeliefBase
import api.query.TestQueryEngine
import api.event.Event
import api.query.GoalQueryEngine
import api.query.BeliefQueryEngine
import api.query.Query

interface PlanMatcher<
        Belief : Any,
        TQ: Query.Test,
        QueryResult,
        Goal : Any,
        GQ: Query.Goal,
        BQ: Query.Belief> {

    val testEngine: TestQueryEngine<Belief, TQ, BeliefBase<Belief>, QueryResult>
    val goalEngine : GoalQueryEngine<Goal, GQ>
    val beliefEngine : BeliefQueryEngine<Belief, BQ>

    //TODO instead of Plan return something else? e.g. plan + PlanContext?
    // or let the handler of this function manage the invocation of the plan with the appropriate parameters?
    fun matchPlan(
        event: Event.Internal,
        beliefs: BeliefBase<Belief>,
        plans: List<Plan<PlanTrigger, TQ, Any>>,
    ) : Plan<PlanTrigger, TQ, Any>?

    companion object {
        fun <
            Belief : Any,
            TQ: Query.Test,
            QueryResult,
            Goal : Any,
            GQ: Query.Goal,
            BQ: Query.Belief> of(
            testEngine: TestQueryEngine<Belief, TQ, BeliefBase<Belief>, QueryResult>,
            goalEngine: GoalQueryEngine<Goal, GQ>,
            beliefEngine: BeliefQueryEngine<Belief, BQ>
        ): PlanMatcher<Belief, TQ, QueryResult, Goal, GQ, BQ> =
            object : PlanMatcher<Belief, TQ, QueryResult, Goal, GQ, BQ> {
                override val testEngine = testEngine
                override val goalEngine = goalEngine
                override val beliefEngine = beliefEngine

                override fun matchPlan(
                    event: Event.Internal,
                    beliefs: BeliefBase<Belief>,
                    plans: List<Plan<PlanTrigger, TQ, Any>>
                ): Plan<PlanTrigger, TQ, Any>? =
                    plans.filter{
                        // Select relevant plans
                        //TODO Why all the casts? And why is it breaking when moving the "relevantFor" logic inside the plan Trigger?
                        when(it.trigger) {
                            is PlanTrigger.Goal.Achieve<*, *> -> event is Event.Internal.Goal.Achieve<*, *>
                                    && goalEngine((it.trigger as PlanTrigger.Goal.Achieve<*, *>).query as GQ,
                                event.goal as Goal
                            ) != null
                            is PlanTrigger.Belief<*, *> -> event is Event.Internal.Belief<*>
                                    && beliefEngine((it.trigger as PlanTrigger.Belief<*, *>).query as BQ,
                                event.belief as Belief
                            ) != null

                            is PlanTrigger.Goal.Test<*> -> event is Event.Internal.Goal.Test<*, *>
                                    && (it.trigger as PlanTrigger.Goal.Test<*>).query == event.query
                        }
                    }
//                    .filter {
//                        when (it.trigger) {
//                            is PlanTrigger.Goal.Achieve<*, *> -> (it.trigger as PlanTrigger.Goal.Achieve<*, *>)
//                                .relevantFor(event, goalEngine)
//
//                            is PlanTrigger.Belief<*, *> -> (it.trigger as PlanTrigger.Belief<*, *>)
//                                .relevantFor(event, beliefEngine)
//
//                            is PlanTrigger.Goal.Test<*> -> (it.trigger as PlanTrigger.Goal.Test<*>)
//                                .relevantFor(event)
//                        }
//                    }
                    .firstOrNull {
                        testEngine(it.guard, beliefs) != null //applicable plans
                    }
            }
    }
}
