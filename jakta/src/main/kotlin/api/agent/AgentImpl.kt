package api.agent

import api.belief.BeliefBase
import api.environment.Environment
import api.environment.EnvironmentContext
import api.event.Event
import api.event.GoalAddEvent
import api.intention.Intention
import api.intention.IntentionInterceptor
import api.intention.MutableIntentionPool
import api.intention.MutableIntentionPoolImpl
import api.plan.GuardScope
import api.plan.Plan
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.reflect.KType

open class AgentImpl<Belief : Any, Goal : Any, Env : Environment>(
    initialBeliefs: Collection<Belief>,
    initialGoals: List<Goal>,
    override val beliefPlans: List<Plan.Belief<Belief, Goal, Env, *, *>>,
    override val goalPlans: List<Plan.Goal<Belief, Goal, Env, *, *>>,
    override val id: AgentID = AgentID(),
    private val events: Channel<Event.Internal> = Channel(Channel.UNLIMITED),
) : Agent<Belief, Goal, Env>,
    AgentActions<Belief, Goal>,
    GuardScope<Belief>,
    SendChannel<Event.Internal> by events {
    private val log =
        Logger(
            Logger.config,
            "Agent[${id.id}]",
        )

    override val beliefs: Collection<Belief>
        get() = beliefBase.snapshot()

    private val beliefBase: BeliefBase<Belief> = BeliefBase.of(this, initialBeliefs)
    private val intentionPool: MutableIntentionPool = MutableIntentionPoolImpl(this)

    init {
        initialGoals.forEach { alsoAchieve(it) }
    }

    override suspend fun step(scope: CoroutineScope) {
        log.i { "waiting for event..." }
        val event = events.receive()
        log.i { "received event: $event" }
        when (event) {
            // TODO per rimuovere questo cast dovrei tipare Event.Internal con Belief e Goal (si può fare ma è subottimo?)
            is Event.Internal.Belief<*> -> scope.handleBeliefEvent(event as Event.Internal.Belief<Belief>)
            is Event.Internal.Goal<*, *> -> scope.handleGoalEvent(event as Event.Internal.Goal<Goal, Any?>)
            is Event.Internal.Step -> handleStepEvent(event)
        }
    }

    /**
     * Launches plans in the SupervisorScope of the Step.
     * @param event the belief event that triggered the plan execution.
     */
    private suspend fun CoroutineScope.handleBeliefEvent(event: Event.Internal.Belief<Belief>) {
        val plan =
            selectPlan(
                entity = event.belief,
                entityMessage = "belief",
                planList = beliefPlans,
                relevantFilter = {
                    when (event) {
                        is Event.Internal.Belief.Add<Belief> -> it is Plan.Belief.Addition
                        is Event.Internal.Belief.Remove<Belief> -> it is Plan.Belief.Removal
                    } && it.isRelevant(event.belief)
                },
                applicableFilter = {
                    it.isApplicable(this@AgentImpl, event.belief)
                },
            ) ?: run {
                log.e { "No plan selected for belief event $event" }
                handleFailure(event, IllegalArgumentException("No plan selected for belief event $event"))
                return
            }

        launchPlan(event, event.belief, plan)
    }

    // TODO In order to be capable to complete the completion, i had to remove the star projection and put Any?
    //  This requires refactoring of type management
    private suspend fun CoroutineScope.handleGoalEvent(event: Event.Internal.Goal<Goal, Any?>) {
        val plan =
            selectPlan(
                entity = event.goal,
                entityMessage = "goal",
                planList = goalPlans,
                relevantFilter = {
                    when (event) {
                        is Event.Internal.Goal.Add<Goal, *> -> it is Plan.Goal.Addition
                        is Event.Internal.Goal.Remove<Goal, *> -> it is Plan.Goal.Removal
                        is Event.Internal.Goal.Failed<Goal, *> -> it is Plan.Goal.Failure
                    } && it.isRelevant(event.goal)
                },
                applicableFilter = {
                    it.isApplicable(this@AgentImpl, event.goal)
                },
            ) ?: run {
                log.e { "No plan selected for goal event $event" }
                handleFailure(event, IllegalArgumentException("No plan selected for goal event $event"))
                return
            }

        launchPlan(event, event.goal, plan, event.completion)
    }

    private suspend fun <TriggerEntity : Any> CoroutineScope.launchPlan(
        event: Event.Internal,
        entity: TriggerEntity,
        plan: Plan<Belief, Goal, Env, TriggerEntity, *, *>,
        completion: CompletableDeferred<Any?>? = null, // TODO Check if this Any? can be improved
    ) {
        log.d { "Launching plan $plan for event $event" }
        val environment: Env = currentCoroutineContext()[EnvironmentContext]?.environment as Env
        val intention = intentionPool.nextIntention(event)

        launch(IntentionInterceptor + intention + intention.job) {
            try {
                log.d { "Running plan $plan" }
                val result = plan.run(this@AgentImpl, this@AgentImpl, environment, entity)
                completion?.complete(result)
            } catch (e: Exception) {
                handleFailure(event, e)
            }
        }
        log.d { "Launched plan $plan" }
    }

    private fun <TriggerEntity : Any> selectPlan(
        entity: TriggerEntity,
        entityMessage: String,
        planList: List<Plan<Belief, Goal, Env, TriggerEntity, *, *>>,
        relevantFilter: (Plan<Belief, Goal, Env, TriggerEntity, *, *>) -> Boolean,
        applicableFilter: (Plan<Belief, Goal, Env, TriggerEntity, *, *>) -> Boolean,
    ): Plan<Belief, Goal, Env, TriggerEntity, *, *>? {
        val relevant = planList.filter(relevantFilter)

        if (relevant.isEmpty()) {
            log.e { "No relevant plans for $entityMessage: $entity" }
        }

        val applicable = relevant.filter(applicableFilter)

        if (applicable.isEmpty()) {
            log.e { "No applicable plans for $entityMessage: $entity" }
        }

        return applicable.firstOrNull()?.let {
            log.d { "Selected plan $it for $entityMessage: $entity" }
            it
        }
    }

    private suspend fun handleFailure(
        event: Event.Internal,
        e: Exception,
    ) {
        log.e { "Plan execution failed for event $event with exception: ${e.message}" }
    }

    // TODO(Missing implementation for greedy event selection in case Step.intention was removed from intention pool)
    private suspend fun handleStepEvent(event: Event.Internal.Step) {
        log.d { "Handling step event for intention ${event.intention.id.id}" }
        intentionPool.stepIntention(event)
    }

    @Deprecated("Use achieve instead", replaceWith = ReplaceWith("achieve(goal)"), level = DeprecationLevel.ERROR)
    override suspend fun <PlanResult> internalAchieve(
        goal: Goal,
        resultType: KType,
    ): PlanResult {
        val completion = CompletableDeferred<PlanResult>()
        val intention = currentCoroutineContext()[Intention]

        check(intention != null) { "Cannot happen that an achieve invocation comes from a null intention." }

        log.d { "Achieving $goal. Previous intention $intention" }
        events.trySend(GoalAddEvent(goal, resultType, completion, intention))
        return completion.await() // Blocking the continuation
    }

    override fun print(message: String) {
        log.a { message }
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
        // TODO not sure this is ok
        // Am I killing the MAS?
        log.d { "Terminating agent" }
        currentCoroutineContext()
            .job.parent
            ?.parent
            ?.cancel()
    }
}
