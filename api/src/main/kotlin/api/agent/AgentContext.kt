package api.agent

/**
 * This interface offers internal actions that can be used in plans.
 */
interface AgentContext<Belief: Any> {
    /**
     * Logs a message
     */
    fun say (message: String)

    /**
     * Adds an event to the agent's queue to achieve a goal and suspends until the goal is achieved.
     */
    suspend fun <PlanResult> achieve(): PlanResult

    /**
     * Adds an event to the agent's queue to achieve a goal and don't wait for it to complete.
     */
    suspend fun alsoAchieve() : Unit

    /**
     * Forcefully fail the current plan being pursued.
     */
    fun fail()

    /**
     * Forcefully fail the current plan being pursued.
     */
    fun success()

    /**
     * Add the belief to the agent's belief base (eventually generating events).
     */
    suspend fun believe(belief : Belief)

    /**
     * Remove the belief from the agent's belief base (eventually generating events).
     */
    suspend fun forget(belief : Belief)

}