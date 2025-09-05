package dsl

import kotlinx.coroutines.Deferred
import kotlin.reflect.KType
import kotlin.reflect.typeOf

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
    val beliefs: Collection<Belief>
    //TODO FIX GENERICS:
    // the first is (Goal | Belief) but the other ones?
    // I think it is ok if they are Any as we won't have restrictions and each plan can have:
    // - custom trigger result (?)
    // - custom guard result i.e. context, which can also be simply TriggerResult when there is no guard so basically Context = (TriggerResult | Context)
    // - custom plan result (which we store as a KType to find plans with reflection)

    val plans: List<Plan<Belief, Goal, Env, Any, Any, Any, Any>>
    suspend fun <PlanResult> achieve(goal: Goal) : PlanResult
}

//TODO THIS IS THE SOURCE OF ALL ISSUES
// Should we build and keep two parallel collections of plans? One for beliefs and one for goals?
// Note that this will only solve the issue of the "TriggerEntity" generic
// Which I have already fixed with a split PlanBuilder
sealed interface Plan<Belief : Any, Goal: Any,  Env : Environment, TriggerEntity : Any, TriggerResult : Any, Context: Any, PlanResult> {
    val trigger: (TriggerEntity) -> TriggerResult?
    val guard : ((Collection<Belief>, TriggerResult) -> Context?)?
    val body :  suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult
    val resultType : KType

    fun isRelevant(e: TriggerEntity, desiredResult: KType) : Boolean = resultType == desiredResult && this.trigger(e) != null

    fun isApplicable(beliefs: Collection<Belief>, e : TriggerEntity, desiredResult: KType) : Boolean = resultType == desiredResult &&
        when (val trig = trigger(e)) {
            null -> false
            else -> when (val g = guard) {
                null -> true
                else -> g(beliefs, trig) != null
            }
        }

    fun getPlanScope(beliefs: Collection<Belief>, e : TriggerEntity) : PlanScope<Belief, Goal, Env, Context>


    interface Belief<Belief : Any, Goal: Any,  Env : Environment, TriggerResult : Any, Context: Any, PlanResult>
        : Plan<Belief, Goal, Env, Belief, TriggerResult, Context, PlanResult>

    interface Goal<Belief : Any, Goal: Any,  Env : Environment, TriggerResult : Any, Context: Any, PlanResult>
        : Plan<Belief, Goal, Env, Goal, TriggerResult, Context, PlanResult>
}



/// HYBRID MODEL/DSL ENTITIES

@JaktaDSL
interface PlanScope<Belief : Any, Goal: Any, Env : Environment, Context : Any> {
    val agent: Agent<Belief, Goal, Env> //TODO probably a different interface with only the "legal" side effects
    val environment: Env
    val context : Context
}

@JaktaDSL
interface GuardScope<Belief : Any> {
    val beliefs : Collection<Belief>
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
        : PlanBuilder.Belief<Belief, Goal, Env, BeliefQueryResult, PlanResult>
    }
    sealed interface OnGoal<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any>
        : TriggerBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult> {

        fun <PlanResult> goal(goalQuery: Goal.() -> GoalQueryResult?)
        : PlanBuilder.Goal<Belief, Goal, Env, GoalQueryResult, PlanResult>
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
    ) : Collection<Belief>

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

//TODO I'm not sure all this extra stuff is needed, it seems too much
@JaktaDSL
sealed interface PlanBuilder<Belief : Any, Goal: Any, Env : Environment, Context : Any, PlanResult> {

    interface Belief<Belief : Any, Goal: Any, Env : Environment, Context : Any, PlanResult> : PlanBuilder<Belief, Goal, Env, Context, PlanResult> {
        infix fun <OutputContext : Any> onlyWhen(guard: GuardScope<Belief>.(Context) -> OutputContext?) :
                GuardedPlanBuilder.Belief<Belief, Goal, Env, Context, OutputContext, PlanResult>

        infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult) :
            Plan.Belief<Belief, Goal, Env, Context, Context, PlanResult>
    }

    interface Goal<Belief : Any, Goal: Any, Env : Environment, Context : Any, PlanResult> : PlanBuilder<Belief, Goal, Env, Context, PlanResult> {
        infix fun <OutputContext : Any> onlyWhen(guard: GuardScope<Belief>.(Context) -> OutputContext?) :
                GuardedPlanBuilder.Goal<Belief, Goal, Env, Context, OutputContext, PlanResult>

        infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult) :
            Plan.Goal<Belief, Goal, Env, Any, Context, PlanResult>
    }
}

// TODO This is definitely duplicating some logic but now I have fully bounded types
sealed interface GuardedPlanBuilder<Belief : Any, Goal: Any, Env : Environment, TriggerContext : Any, OutputContext : Any, PlanResult> {

    interface Belief<Belief : Any, Goal: Any, Env : Environment, TriggerContext : Any, OutputContext : Any, PlanResult> :
        GuardedPlanBuilder<Belief, Goal, Env, TriggerContext, OutputContext, PlanResult> {

        infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, OutputContext>.() -> PlanResult) :
                Plan.Belief<Belief, Goal, Env, TriggerContext, OutputContext, PlanResult>
    }

    interface Goal<Belief : Any, Goal: Any, Env : Environment, TriggerContext : Any, OutputContext : Any, PlanResult> :
        GuardedPlanBuilder<Belief, Goal, Env, TriggerContext, OutputContext, PlanResult> {

        infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, OutputContext>.() -> PlanResult) :
                Plan.Goal<Belief, Goal, Env, TriggerContext, OutputContext, PlanResult>
    }
}

//////////////////////////////////////////////////////////////////////
// DSL IMPL
//////////////////////////////////////////////////////////////////////


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