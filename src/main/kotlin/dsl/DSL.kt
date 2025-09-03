package dsl

import kotlinx.coroutines.CoroutineScope

/*

adding.belief(pluto) onlyIf {
    // Dà un booleano e un "contesto" (plancontext?)
} triggers {
}
adding.goal(eat(pluto)) triggers {
}
removing.belief(pluto) triggers {
}
removing.goal(eat(pluto)) triggers {
}
failing.goal(eat(pluto)) triggers {
}
retrieve(pluto) triggers { // NO
}
failing.retrieve(pluto) triggers { // NO
}

 */
//////////////////////////////////////////////////////////////////////
// MODEL ENTITIES
//////////////////////////////////////////////////////////////////////

interface Agent<Belief : Any> {
    val beliefs: Set<Belief>
}

interface Environment

@JaktaDSL
interface PlanScope<Belief : Any> : CoroutineScope {
    val agent: Agent<Belief>
    val environment: Any //Env
}

@JaktaDSL
interface GuardScope<Belief : Any> {
    val beliefs : Set<Belief>
}


//////////////////////////////////////////////////////////////////////
// DSL
//////////////////////////////////////////////////////////////////////

@DslMarker
annotation class JaktaDSL

@JaktaDSL
sealed interface TriggerScope<Belief : Any, BeliefQueryResult : Any, GoalQueryResult : Any> {
    sealed interface OnBelief<Belief : Any , BeliefQueryResult : Any, GoalQueryResult : Any>
        : TriggerScope<Belief, BeliefQueryResult, GoalQueryResult> {

        fun <PlanResult> belief(beliefQuery: Belief.() -> BeliefQueryResult?)
        : PlanConstructor<Belief, BeliefQueryResult, PlanResult>
    }
    sealed interface OnGoal<Belief : Any, Goal : Any, BeliefQueryResult : Any, GoalQueryResult : Any>
        : TriggerScope<Belief, BeliefQueryResult, GoalQueryResult> {

        fun <PlanResult> goal(goalQuery: Goal.() -> GoalQueryResult?)
        : PlanConstructor<Belief, GoalQueryResult, PlanResult>
    }
}

sealed interface Addition<Belief : Any, Goal : Any, BeliefQueryResult : Any, GoalQueryResult : Any> :
    TriggerScope.OnBelief<Belief, BeliefQueryResult, GoalQueryResult>,
    TriggerScope.OnGoal<Belief, Goal, BeliefQueryResult, GoalQueryResult>

sealed interface Removal<Belief : Any, Goal : Any, BeliefQueryResult : Any, GoalQueryResult : Any> :
    TriggerScope.OnBelief<Belief, BeliefQueryResult, GoalQueryResult>,
    TriggerScope.OnGoal<Belief, Goal, BeliefQueryResult, GoalQueryResult>

sealed interface FailureInterception<Belief : Any, Goal : Any, BeliefQueryResult : Any, GoalQueryResult : Any> :
    TriggerScope.OnGoal<Belief, Goal, BeliefQueryResult, GoalQueryResult>

@JaktaDSL
interface PlanLibraryConstructor<Belief : Any, Goal : Any, BeliefQueryResult : Any, GoalQueryResult : Any> {
    val adding: Addition<Belief, Goal, BeliefQueryResult, GoalQueryResult> get() = TODO()
    val removing: Removal<Belief, Goal, BeliefQueryResult, GoalQueryResult> get() = TODO()
    val failing: FailureInterception<Belief, Goal, BeliefQueryResult, GoalQueryResult> get() = TODO()

}

@JaktaDSL
interface AgentConstructor<Belief : Any, Goal : Any, BeliefQueryResult : Any, GoalQueryResult : Any> {

    fun believes(
        block: BeliefConstructor<Belief>.() -> Unit
    ) : Unit = TODO()

    fun hasInitialGoals(
        block: GoalConstructor<Goal>.() -> Unit
    ) : Unit = TODO()

    fun hasPlans(
        block: PlanLibraryConstructor<Belief, Goal, BeliefQueryResult, GoalQueryResult>.() -> Unit
    ): Unit = TODO()
}

@JaktaDSL
interface PlanConstructor<Belief : Any, Context, PlanResult> {
    infix fun <OutputContext : Any> onlyWhen(guard: GuardScope<Belief>.(Context) -> OutputContext?): PlanConstructor<Belief, OutputContext, PlanResult> = TODO()
    infix fun triggers(body: suspend PlanScope<Belief>.(Context) -> PlanResult): Unit = TODO()
}

@JaktaDSL
interface BeliefConstructor<Belief : Any> {
    operator fun Belief.unaryPlus() : Unit = TODO()
}

@JaktaDSL
interface GoalConstructor<Belief : Any> {
    operator fun Belief.not() : Unit = TODO()
}


// ENTRYPOINT
@JaktaDSL
fun <Belief : Any, Goal : Any, BeliefQueryResult : Any, GoalQueryResult : Any> agent(
    block: AgentConstructor<Belief, Goal, BeliefQueryResult, GoalQueryResult>.() -> Unit
): PlanLibraryConstructor<Belief, Goal, BeliefQueryResult, GoalQueryResult> = TODO()



//////////////////////////////////////////////////////////////////////
// DSL INCARNATION
//////////////////////////////////////////////////////////////////////



fun main() {
    agent {
        believes {
            + "pippo"
            + "pluto"
        }
        hasInitialGoals {
            ! 27
        }
        hasPlans {
            adding.belief{
                Regex("pluto").matchEntire(this)
            } onlyWhen {
                listOfNotNull(it).takeIf { "pippo" in beliefs }
            } triggers { ctx ->
                agent.beliefs.contains("???")
                ctx.all { it.groups.isNotEmpty() }
            }
            adding.goal {
                takeIf { this > 27 }
            } triggers { ctx ->
                val x = ctx + 10
            }
        }
        
    }
    
}