package dsl

//////////////////////////////////////////////////////////////////////
// MODEL ENTITIES
//////////////////////////////////////////////////////////////////////

interface MAS<Belief : Any, Goal : Any, Env : Environment >{
    val environment: Env
    val agents : Set<Agent<Belief, Goal, Env>>

    fun run() : Unit
}

interface Environment

interface Agent<Belief : Any, Goal: Any,  Env : Environment> {
    val beliefs: Set<Belief>
    val plans: Sequence<Plan<Belief, Goal, Env, Any, Any, Any, Any>>  // TODO FIX GENERICS
    fun <PlanResult> achieve(goal: Goal) : PlanResult
}

//TODO THIS IS THE SOURCE OF ALL ISSUES
// Should we build and keep two parallel collections of plans? One for beliefs and one for goals?
sealed interface Plan<Belief : Any, Goal: Any,  Env : Environment, TriggerEntity : Any, TriggerResult : Any, Context: Any, PlanResult> {
    val trigger: Query<TriggerEntity, TriggerResult>
    val guard : Guard<Belief, TriggerResult, Context>?
    val body : PlanBody<Belief, Goal, Env, Context, PlanResult>
//
//    interface Belief<Belief : Any, Goal: Any,  Env : Environment, TriggerResult : Any, Context: Any, PlanResult>
//        : Plan<Belief, Goal, Env, Belief, TriggerResult, Context, PlanResult>
//
//    interface Goal<Belief : Any, Goal: Any,  Env : Environment, TriggerResult : Any, Context: Any, PlanResult>
//        : Plan<Belief, Goal, Env, Goal, TriggerResult, Context, PlanResult>

}

fun interface Query<Entity : Any, Result: Any> : (Entity) -> Result?

fun interface Guard<Belief: Any, InputContext : Any, OutputContext : Any> : (Belief, InputContext) -> OutputContext?

fun interface PlanBody<Belief : Any, Goal : Any, Env : Environment, Context : Any, PlanResult> : suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult

/// HYBRID MODEL/DSL ENTITIES

@JaktaDSL
interface PlanScope<Belief : Any, Goal: Any, Env : Environment, Context : Any> {
    val agent: Agent<Belief, Goal, Env>
    val environment: Env
    val context : Context
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
sealed interface TriggerBuilder<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> {
    sealed interface OnBelief<Belief : Any , Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any>
        : TriggerBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult> {

        fun <PlanResult> belief(beliefQuery: Belief.() -> BeliefQueryResult?)
        : PlanBuilder<Belief, Goal, Env, BeliefQueryResult, PlanResult>
    }
    sealed interface OnGoal<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any>
        : TriggerBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult> {

        fun <PlanResult> goal(goalQuery: Goal.() -> GoalQueryResult?)
        : PlanBuilder<Belief, Goal, Env, GoalQueryResult, PlanResult>
    }
}

sealed interface Addition<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> :
    TriggerBuilder.OnBelief<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>,
    TriggerBuilder.OnGoal<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>

sealed interface Removal<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> :
    TriggerBuilder.OnBelief<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>,
    TriggerBuilder.OnGoal<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>

sealed interface FailureInterception<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> :
    TriggerBuilder.OnGoal<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>


@JaktaDSL
interface MasBuilder<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> {

    @JaktaDSL
    fun agent(
        block: AgentBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>.() -> Unit
    ): Agent<Belief, Goal, Env>

    fun environment (block: () -> Env)
}

@JaktaDSL
interface AgentBuilder<Belief : Any, Goal : Any, Env: Environment, BeliefQueryResult : Any, GoalQueryResult : Any> {

    fun believes(
        block: BeliefBuilder<Belief>.() -> Unit
    ) : Set<Belief>

    fun hasInitialGoals(
        block: GoalBuilder<Goal>.() -> Unit
    ) : Sequence<Goal>

    fun hasPlans(
        block: PlanLibraryBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>.() -> Unit
    ): Sequence<Plan<Belief, Goal, Env, Any, Any, Any, Any>>  // TODO FIX GENERICS
}

@JaktaDSL
interface BeliefBuilder<Belief : Any> {
    operator fun Belief.unaryPlus()
}

@JaktaDSL
interface GoalBuilder<Goal : Any> {
    operator fun Goal.not()
}

@JaktaDSL
interface PlanLibraryBuilder<Belief : Any, Goal : Any, Env: Environment, BeliefQueryResult : Any, GoalQueryResult : Any> {
    val adding: Addition<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>
    val removing: Removal<Belief, Goal, Env,  BeliefQueryResult, GoalQueryResult>
    val failing: FailureInterception<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>
}

@JaktaDSL
interface PlanBuilder<Belief : Any, Goal: Any, Env : Environment, Context : Any, PlanResult> {
    infix fun <OutputContext : Any> onlyWhen(guard: GuardScope<Belief>.(Context) -> OutputContext?) :
            PlanBuilder<Belief, Goal, Env, OutputContext, PlanResult>
    infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult) :
            Plan<Belief, Goal, Env, Any, Any, Context, PlanResult>  //TODO FIX GENERICS
}


// ENTRYPOINT
@JaktaDSL
fun <Belief : Any, Goal : Any,  Env: Environment, BeliefQueryResult : Any, GoalQueryResult : Any> mas(
    block: MasBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>.() -> Unit
): MAS<Belief, Goal, Env> = TODO()

//TODO I want more entrypoints, to be able to define stuff in multiple files..
// this should be easy, we can do it later


//////////////////////////////////////////////////////////////////////
// DSL INCARNATION
//////////////////////////////////////////////////////////////////////

class TestEnvironment : Environment {
    fun test(): Unit {}
}



fun main() {
    mas {
        environment {
            TestEnvironment()
        }
        agent {

            believes {
                + "pippo"
                + "pluto"
            }

            hasInitialGoals {
                ! 27
            }

            hasPlans {

                adding.belief {
                    Regex("pluto").matchEntire(this)
                } onlyWhen {
                    listOfNotNull(it).takeIf { "pippo" in beliefs }
                } triggers {
                    agent.beliefs.contains("???")
                    context.all { it.groups.isNotEmpty() }
                }

                adding.goal {
                    takeIf{it >= 27}
                } triggers {
                    val x = context + 10
                    environment.test()
                    val result : String = agent.achieve(23)
                }
            }
        }
    }.run()
}