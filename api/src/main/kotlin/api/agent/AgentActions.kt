package api.agent

/**
 * This interface offers internal actions that can be used in plans.
 */
interface AgentActions<Belief: Any, Goal: Any> {
    /**
     * Logs a message
     */
    fun print (message: String)

    /**
     * Adds an event to the agent's queue to achieve a goal and suspends until the goal is achieved.
     */
    suspend fun <PlanResult> achieve(goal: Goal) : PlanResult

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

    /**
     * Add the belief to the agent's belief base (eventually generating events).
     */
    suspend fun believe(belief : Belief)

    /**
     * Remove the belief from the agent's belief base (eventually generating events).
     */
    suspend fun forget(belief : Belief)

}