package dsl

// ---------- Builders (DSL) ----------

private class MasBuilderImpl<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> :
    MasBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult> {

    private var envFactory: (() -> Env)? = null
    private val builtAgents = mutableListOf<Agent<Belief, Goal, Env>>()

    override fun agent(
        block: AgentBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>.() -> Unit
    ): Agent<Belief, Goal, Env> {
        val agentBuilder = AgentBuilderImpl<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>(
            environmentProvider = { envFactory?.invoke() ?: error("Environment not defined yet") }
        )
        val agent = agentBuilder.apply(block).build()
        builtAgents += agent
        return agent
    }

    override fun environment(block: () -> Env) {
        envFactory = block
    }

    fun build(): MAS<Belief, Goal, Env> {
        val env = envFactory?.invoke() ?: error("Environment must be provided")
        return MASImpl(env, builtAgents.toSet())
    }
}

private class AgentBuilderImpl<
        Belief : Any,
        Goal : Any,
        Env : Environment,
        BeliefQueryResult : Any,
        GoalQueryResult : Any
        >(
    private val environmentProvider: () -> Env
) : AgentBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult> {

    private val beliefsList = mutableListOf<Belief>()
    private val initialGoals = mutableListOf<Goal>()
    private val planList = mutableListOf<Plan<Belief, Goal, Env, Any, Any, Any, Any>>()

    // Will be set once agent is created, enabling late-binding from plans
    private lateinit var agentRef: AgentImpl<Belief, Goal, Env>

    override fun believes(block: BeliefBuilder<Belief>.() -> Unit): Collection<Belief> {
        val bb = BeliefBuilderImpl(beliefsList)
        bb.apply(block)
        return beliefsList
    }

    override fun hasInitialGoals(block: GoalBuilder<Goal>.() -> Unit): Sequence<Goal> {
        val gb = GoalBuilderImpl(initialGoals)
        gb.apply(block)
        return initialGoals.asSequence()
    }

    override fun hasPlans(
        block: PlanLibraryBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>.() -> Unit
    ): Sequence<Plan<Belief, Goal, Env, Any, Any, Any, Any>> {
        val plb = PlanLibraryBuilderImpl<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>()
        plb.apply(block)
        // planList += plb.builtPlans TODO
        return planList.asSequence()
    }

    fun build(): Agent<Belief, Goal, Env> {
        val env = environmentProvider()
        val agent = AgentImpl(beliefsList.toList(), planList.toList())
        agentRef = agent
        return agent
    }
}

private class BeliefBuilderImpl<Belief : Any>(
    private val sink: MutableCollection<Belief>
) : BeliefBuilder<Belief> {
    override operator fun Belief.unaryPlus() {
        sink += this
    }
}

private class GoalBuilderImpl<Goal : Any>(
    private val sink: MutableCollection<Goal>
) : GoalBuilder<Goal> {
    override operator fun Goal.not() {
        sink += this
    }
}

private class PlanLibraryBuilderImpl<Belief : Any, Goal : Any, Env: Environment, BeliefQueryResult : Any, GoalQueryResult : Any> :
    PlanLibraryBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult> {
    override val adding: Addition<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult> = TriggerImpl.Addition(::addPlan)
    override val removing: Removal<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult> = TriggerImpl.Removal(::addPlan)
    override val failing: FailureInterception<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult> = TriggerImpl.FailureInterception(::addPlan)

    val builtPlans = mutableListOf<Plan<Belief, Goal, Env, Any, Any, Any, Any>>()

    private fun addPlan(plan: Plan<Belief, Goal, Env, Any, Any, Any, Any>) {
        builtPlans += plan
    }
}

private abstract class TriggerImpl<Belief : Any, Goal : Any, Env: Environment, BeliefQueryResult : Any, GoalQueryResult : Any>(
    val addPlanFunction: (plan: Plan<Belief, Goal, Env, Any, Any, Any, Any>) -> Unit
) :
    Addition<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>,
    Removal<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>,
    FailureInterception<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>{
    override fun <PlanResult> belief(beliefQuery: Belief.() -> BeliefQueryResult?): PlanBuilder.Belief<Belief, Goal, Env, BeliefQueryResult, PlanResult> {
        TODO()
    }

    override fun <PlanResult> goal(goalQuery: Goal.() -> GoalQueryResult?): PlanBuilder.Goal<Belief, Goal, Env, GoalQueryResult, PlanResult> {
        TODO()
    }

    class Addition<Belief : Any, Goal : Any, Env: Environment, BeliefQueryResult : Any, GoalQueryResult : Any>(
        addPlanFunction: (plan: Plan<Belief, Goal, Env, Any, Any, Any, Any>) -> Unit
    ) : TriggerImpl<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>(addPlanFunction)

    class Removal<Belief : Any, Goal : Any, Env: Environment, BeliefQueryResult : Any, GoalQueryResult : Any>(
        addPlanFunction: (plan: Plan<Belief, Goal, Env, Any, Any, Any, Any>) -> Unit
    ) : TriggerImpl<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>(addPlanFunction)

    class FailureInterception<Belief : Any, Goal : Any, Env: Environment, BeliefQueryResult : Any, GoalQueryResult : Any>(
        addPlanFunction: (plan: Plan<Belief, Goal, Env, Any, Any, Any, Any>) -> Unit
    ) : TriggerImpl<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>(addPlanFunction)
}


private abstract class PlanBuilderImpl<Belief : Any, Goal: Any, Env : Environment, Context : Any, PlanResult>() :
    PlanBuilder<Belief, Goal, Env, Context, PlanResult>






//////////////////////////////////////////////////////////////////////
// ENTRYPOINT
//////////////////////////////////////////////////////////////////////

@JaktaDSL
fun <Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> mas(
    block: MasBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>.() -> Unit
): MAS<Belief, Goal, Env> {
    val mb = MasBuilderImpl<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>()
    mb.apply(block)
    return mb.build()
}

//TODO I want more entrypoints, to be able to define stuff in multiple files..
// this should be easy, we can do it later

