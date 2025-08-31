package api.agent

import api.belief.BeliefBase
import api.event.Event
import api.intention.IntentionInterceptor
import api.intention.IntentionPool
import api.plan.Plan
import api.plan.PlanMatcher
import api.plan.PlanTrigger
import api.query.Query
import api.query.TestQueryEngine
import kotlinx.coroutines.channels.ReceiveChannel

class AgentImpl<
Belief : Any,
TestQuery : Query.Test,
TestQueryResult,
Goal : Any,
GoalQuery : Query.Goal,
BeliefQuery : Query.Belief,
>(
    override val beliefBase: BeliefBase<Belief>,
    override val testQueryEngine: TestQueryEngine<Belief, TestQuery, BeliefBase<Belief>, TestQueryResult>,
    override val plans: List<Plan<PlanTrigger, TestQuery, Any>>,
    override val planMatcher: PlanMatcher<Belief, TestQuery, TestQueryResult, Goal, GoalQuery, BeliefQuery>,
    override val intentionPool: IntentionPool,
    override val intentionInterceptor: IntentionInterceptor,
    override val events: ReceiveChannel<Event.Internal>,
    override val id: AgentID
) : Agent<Belief, TestQuery, TestQueryResult, Goal, GoalQuery, BeliefQuery, >{


    override fun init() {
        TODO("Not yet implemented")
    }

    override suspend fun step() {
        val event = events.receive()
        when(event) {
            is Event.Internal.Step -> TODO()
            else -> planMatcher.matchPlan(event, beliefBase, plans)?.let {
                //TODO launch plan on the agentContext
            }
        }
    }
}