package api.agent

import api.belief.BeliefBase
import api.environment.Environment
import api.environment.EnvironmentContext
import api.event.Event
import api.event.GoalAddEvent
import api.intention.Intention
import api.intention.IntentionInterceptorImpl
import api.intention.MutableIntentionPool
import api.intention.MutableIntentionPoolImpl
import api.plan.GuardScope
import api.plan.Plan
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KType


open class AgentImpl<Belief : Any, Goal : Any, Env : Environment>(
    val initialBeliefs: Collection<Belief>,
    val initialGoals: List<Goal>,
    override val beliefPlans: List<Plan.Belief<Belief, Goal, Env, *, *>>,
    override val goalPlans: List<Plan.Goal<Belief, Goal, Env, *, *>>,
    override val id: AgentID = AgentID(),
) : Agent<Belief, Goal, Env>,
    AgentActions<Belief, Goal>,
    GuardScope<Belief>
{
    private val events: Channel<Event.Internal> = Channel(Channel.UNLIMITED)
    private val beliefBase: BeliefBase<Belief> = BeliefBase.empty()
    private lateinit var agentScope: CoroutineScope

    private lateinit var agentContext: CoroutineContext

    private val intentionPool: MutableIntentionPool = MutableIntentionPoolImpl()

    override val beliefs: Collection<Belief>
        get() = beliefBase.snapshot()


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun start(scope: CoroutineScope) {
        this.agentScope = scope
        //TODO check that this coroutine goes on without interception..
        agentScope.launch { beliefBase.collect { events.send(it)} }

        agentContext = scope.coroutineContext + IntentionInterceptorImpl(intentionPool, events)

        //Now that everything is setup initial Belief and Goals so that the agent can start working
        beliefBase.addAll(initialBeliefs)
        initialGoals.forEach { alsoAchieve(it) }
    }

    override suspend fun step() {
        println("Waiting for the next event to execute... ")
        val event = events.receive()
        println("Agent received event: $event")
        when (event) {
            //TODO per rimuovere questo cast dovrei tipare Event.Internal con Belief e Goal (si può fare ma è subottimo?)
            is Event.Internal.Belief<*> -> handleBeliefEvent(event as Event.Internal.Belief<Belief>)
            is Event.Internal.Goal<*, *> -> handleGoalEvent(event as Event.Internal.Goal<Goal, Any?>)
            is Event.Internal.Step -> handleStepEvent(event)
        }
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

        val plan = applicablePlans.first() //TODO support other strategies for selecting the plan to execute

        val environment: Env = currentCoroutineContext()[EnvironmentContext]?.environment as Env

        val intentionContext = with(intentionPool) {
            agentScope.nextIntention(event)
        }

        // intentionContext.job -> The execution is children of the intention which executes that event
        // Job is the lifecycle of the coroutine, it manages the cancellation chain.
        // The plus operations: It automatically replaces the keys in the context of this intention.
        agentScope.launch(agentContext + intentionContext + intentionContext.job) {
            try {
                plan.run(this@AgentImpl, this@AgentImpl, environment, event.belief)
            } catch (_: Exception) {
                handleFailure(event)
            }
        }
    }

    // TODO(In order to be capable to complete the completion, i had to remove the star projection and put Any?)
    // This requires refactoring of type management
    private suspend fun handleGoalEvent(event: Event.Internal.Goal<Goal, Any?>) {
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

        val plan = applicablePlans.first() //TODO support other strategies for selecting the plan to execute
        val environment: Env = currentCoroutineContext()[EnvironmentContext]?.environment as Env
        val intentionContext = with(intentionPool) {
            agentScope.nextIntention(event)
            //agentScope.nextIntention(event.intention)
        }

        agentScope.launch(agentContext + intentionContext + intentionContext.job) {
            try {
                val result = plan.run(this@AgentImpl, this@AgentImpl, environment, event.goal)
                event.completion?.complete(result)
            } catch (_: Exception) {
                handleFailure(event)
            }
        }
    }

    private suspend fun handleFailure(event: Event.Internal) {
        TODO("fail")
    }

    // TODO(Missing implementation for greedy event selection in case Step.intention was removed from intention pool)
    private suspend fun handleStepEvent(event: Event.Internal.Step) {
        intentionPool.stepIntention(event)
    }


    @Deprecated("Use achieve instead", replaceWith = ReplaceWith("achieve(goal)"), level = DeprecationLevel.ERROR)
    override suspend fun <PlanResult> _achieve(goal: Goal, resultType: KType): PlanResult {
        val completion = CompletableDeferred<PlanResult>()
        val intention = currentCoroutineContext()[Intention]
        check(intention != null) { "Cannot happen that an achieve invocation comes from a null intention." }
        println("Achieving $goal. Previous intention $intention")
        events.trySend(GoalAddEvent(goal, resultType, completion, intention))
        return completion.await() // Blocking the continuation
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

    override suspend fun terminate() = stop()

    override suspend fun stop() {
        println("Terminating job: " + currentCoroutineContext().job)
        println("Parent: " + currentCoroutineContext().job.parent?.toString())
        println("AgentScope job: " +  agentScope.coroutineContext.job)

        agentScope.coroutineContext.job.cancel()
    }

}
