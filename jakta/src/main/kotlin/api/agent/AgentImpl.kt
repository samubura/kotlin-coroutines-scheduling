package api.agent

import api.belief.BeliefBase
import api.environment.Environment
import api.event.Event
import api.event.GoalAddEvent
import api.plan.GuardScope
import api.plan.Plan
import api.plan.PlanScopeImpl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.reflect.KType


data class AgentImpl<Belief : Any, Goal : Any, Env : Environment>(
    val initialBeliefs: Collection<Belief>,
    val initialGoals: List<Goal>,
    override val beliefPlans: List<Plan.Belief<Belief, Goal, Env, *, *>>, //TODO * Context type is breaking stuff, see handleBeliefEvent
    override val goalPlans: List<Plan.Goal<Belief, Goal, Env, *, *>>,
    override val id: AgentID = AgentID(), //TODO Check
) : Agent<Belief, Goal, Env>,
    AgentActions<Belief, Goal>,
    GuardScope<Belief>
{
    private val events: Channel<Event.Internal> = Channel(Channel.UNLIMITED)
    private val beliefBase: BeliefBase<Belief> = BeliefBase.empty()
    private var agentScope: CoroutineScope? = null

    override val beliefs: Collection<Belief>
        get() = beliefBase.snapshot()


    override fun start(scope: CoroutineScope) {
        check(agentScope != null) { "Agent already initialized" }
        this.agentScope = scope
        //TODO check that this coroutine goes on without interception..
        agentScope?.launch { beliefBase.collect { events.send(it)} }

        //TODO Set the intentionInterceptor in the Scope Context so that all later coroutines inherit it

        //Now that everything is setup initial Belief and Goals so that the agent can start working
        beliefBase.addAll(initialBeliefs)
        initialGoals.forEach {alsoAchieve(it)}
    }

    override suspend fun step() = when(val event = events.receive()) {
        //TODO per rimuovere questo cast dovrei tipare Event.Internal con Belief e Goal (si può fare ma è subottimo?)
        is Event.Internal.Belief<*> -> handleBeliefEvent(event as Event.Internal.Belief<Belief>)
        is Event.Internal.Goal<*,*> -> handleGoalEvent(event as Event.Internal.Goal<Goal, *>)
        is Event.Internal.Step -> handleStepEvent(event)
    }

    private suspend fun handleBeliefEvent(event: Event.Internal.Belief<Belief>) {
        val relevantPlans = beliefPlans.filter { when (event) {
                is Event.Internal.Belief.Add<Belief> -> it is Plan.Belief.Addition
                is Event.Internal.Belief.Remove<Belief> -> it is Plan.Belief.Removal
            } && it.isRelevant(event.belief)
        }
        //TODO signal that there are no relevant plans? For now throw exception
        check(relevantPlans.isNotEmpty()) { "No relevant plans for belief event $event" }

        val applicablePlans = relevantPlans.filter { it.isApplicable(this, event.belief) }
        //TODO signal that there are no applicable plans? For now throw exception
        check(applicablePlans.isNotEmpty()) { "No applicable plans for belief event $event" }

        val plan = applicablePlans.first()
        val context = plan.getPlanContext(this, event.belief)!!
        val environment = TODO("Get the environment, probably from the CoroutineContext?")
        val scope = PlanScopeImpl(this, environment, context)

        // plan.body(scope) //TODO this is not working!

        agentScope!!.launch { plan.run(this@AgentImpl, environment, context) } //TODO neither this!
        //TODO The problem seems to be with the * for the Context type in the Plan list..

    }


    private suspend fun handleGoalEvent(event: Event.Internal.Goal<Goal, *>) {
        val relevantPlans = goalPlans.filter { when (event) {
                is Event.Internal.Goal.Add<Goal, *> -> it is Plan.Goal.Addition
                is Event.Internal.Goal.Remove<Goal, *> -> it is Plan.Goal.Removal
                is Event.Internal.Goal.Failed<Goal, *> -> it is Plan.Goal.Failure
        } && it.isRelevant(event.goal)}

        //TODO signal that there are no relevant plans? For now throw exception
        check(relevantPlans.isNotEmpty()) { "No relevant plans for goal event $event" }


        val applicablePlans = relevantPlans.filter { it.isApplicable(this, event.goal) }
        //TODO signal that there are no applicable plans? For now throw exception
        check(applicablePlans.isNotEmpty()) { "No applicable plans for goal event $event" }
    }

    private suspend fun handleStepEvent(event: Event.Internal.Step) {
        TODO("Not yet implemented")
    }


    @Deprecated("Use achieve instead", replaceWith = ReplaceWith("achieve(goal)"), level = DeprecationLevel.ERROR)
    override suspend fun <PlanResult> _achieve(goal: Goal, resultType: KType): PlanResult {
        val completion = CompletableDeferred<PlanResult>()
        //TODO track intention?
        events.trySend(GoalAddEvent(goal, resultType, completion, null))
        return completion.await()
    }

    override fun print(message: String) {
        println("[$id]: $message")
    }

    override fun alsoAchieve(goal: Goal) {
        events.trySend(GoalAddEvent.withNoResult(goal))
    }

    override fun fail() {
        TODO("Not yet implemented")
    }

    override fun succeed() {
        TODO("Not yet implemented")
    }

    override suspend fun believe(belief: Belief) {
        TODO("Not yet implemented")
    }

    override suspend fun forget(belief: Belief) {
        TODO("Not yet implemented")
    }



}
