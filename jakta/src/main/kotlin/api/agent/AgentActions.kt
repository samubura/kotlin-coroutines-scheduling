package api.agent

import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * This interface offers internal actions that can be used in plans.
 */
interface AgentActions<Belief : Any, Goal : Any> {
    /**
     * Logs a message
     */
    fun print(message: String)

    /**
     * Adds an event to the agent's queue to achieve a goal and suspends until the goal is achieved.
     */
    @Deprecated("Use achieve instead", ReplaceWith("achieve(goal)"), DeprecationLevel.ERROR)
    suspend fun <PlanResult> internalAchieve(
        goal: Goal,
        resultType: KType,
    ): PlanResult

    /**
     * Adds an event to the agent's queue to achieve a goal and don't wait for it to complete.
     */
    fun alsoAchieve(goal: Goal)

    /**
     * Forcefully fail the current plan being pursued.
     */
    fun fail()

    /**
     * Forcefully fail the current plan being pursued.
     */
    fun succeed()

    /** Terminates the execution of the agent **/
    suspend fun terminate()

    /**
     * Add the belief to the agent's belief base (eventually generating events).
     */
    suspend fun believe(belief: Belief)

    /**
     * Remove the belief from the agent's belief base (eventually generating events).
     */
    suspend fun forget(belief: Belief)
}

@Suppress("DEPRECATION_ERROR")
suspend inline fun <B : Any, G : Any, reified PlanResult> AgentActions<B, G>.achieve(goal: G): PlanResult =
    internalAchieve(goal, typeOf<PlanResult>())

// TODO(Ambiguous :((((( as expected. I'd like to avoid to specify the return type of achieve when this is Unit)
// @Suppress("DEPRECATION_ERROR")
// suspend fun <B: Any, G: Any> AgentActions<B, G>.achieve(goal: G) : Unit =
//    _achieve(goal, typeOf<Unit>())
