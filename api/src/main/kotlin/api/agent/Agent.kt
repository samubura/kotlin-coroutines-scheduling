package api.agent

import api.belief.BeliefBase
import api.event.Event
import api.intention.IntentionInterceptor
import api.intention.IntentionPool
import api.query.TestQueryEngine
import api.plan.Plan
import api.plan.PlanMatcher
import api.plan.PlanTrigger
import api.query.Query
import kotlinx.coroutines.channels.ReceiveChannel


/**
 * Represents an agent in a MAS
 */
sealed interface SuspendSteppingAgent {
    val id: AgentID
    fun init()
    suspend fun step()
}


interface Agent<
        Belief : Any,
        TestQuery : Query.Test,
        TestQueryResult,
        Goal : Any,
        GoalQuery : Query.Goal,
        BeliefQuery : Query.Belief,
        > : SuspendSteppingAgent{
    val beliefBase : BeliefBase<Belief>
    val testQueryEngine: TestQueryEngine<Belief, TestQuery, BeliefBase<Belief>, TestQueryResult>
    val plans : List<Plan<PlanTrigger, TestQuery, Any>> // TODO PlanResult is Any...
    val planMatcher : PlanMatcher<Belief, TestQuery, TestQueryResult, Goal, GoalQuery, BeliefQuery>
    val intentionPool: IntentionPool
    val intentionInterceptor : IntentionInterceptor
    val events : ReceiveChannel<Event.Internal>
}
