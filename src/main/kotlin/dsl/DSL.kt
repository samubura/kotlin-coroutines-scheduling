package dsl

import kotlin.reflect.KType
import kotlin.reflect.typeOf

//////////////////////////////////////////////////////////////////////
// DSL
//////////////////////////////////////////////////////////////////////

@DslMarker
annotation class JaktaDSL

@JaktaDSL
interface MasBuilder<Belief : Any, Goal : Any, Env : Environment> {

    @JaktaDSL
    fun agent(block: AgentBuilder<Belief, Goal, Env>.() -> Unit): Agent<Belief, Goal, Env>

    fun environment (block: () -> Env)

    fun build() : MAS<Belief, Goal, Env>
}

@JaktaDSL
interface AgentBuilder<Belief : Any, Goal : Any, Env: Environment> {

    fun believes(block: BeliefBuilder<Belief>.() -> Unit)

    fun hasInitialGoals(block: GoalBuilder<Goal>.() -> Unit)

    fun hasPlans(block: PlanLibraryBuilder<Belief, Goal, Env>.() -> Unit)

    fun addBelief(belief: Belief)

    fun addGoal(goal : Goal)

    fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Env, *, *>)

    fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Env, *, *>)

    fun build(): Agent<Belief, Goal, Env>
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
interface PlanLibraryBuilder<Belief : Any, Goal : Any, Env: Environment> {
    val adding: TriggerBuilder.Addition<Belief, Goal, Env>
    val removing: TriggerBuilder.Removal<Belief, Goal, Env,>
    val failing: TriggerBuilder.FailureInterception<Belief, Goal, Env>

    fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Env, *, *>)
    fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Env, *, *>)
}

@JaktaDSL
sealed interface TriggerBuilder<Belief : Any, Goal : Any, Env : Environment> {

    sealed interface Addition<Belief : Any, Goal : Any, Env : Environment> :
        TriggerBuilder<Belief, Goal, Env>{

        fun <Context : Any> belief(beliefQuery: Belief.() -> Context?)
                : PlanBuilder.Addition.Belief<Belief, Goal, Env, Context>

        fun <Context : Any> goal(goalQuery: Goal.() -> Context?)
                : PlanBuilder.Addition.Goal<Belief, Goal, Env, Context>
    }

    sealed interface Removal<Belief : Any, Goal : Any, Env : Environment> :
        TriggerBuilder<Belief, Goal, Env>{

        fun <Context : Any> belief(beliefQuery: Belief.() -> Context?)
                : PlanBuilder.Removal.Belief<Belief, Goal, Env, Context>

        fun <Context : Any> goal(goalQuery: Goal.() -> Context?)
                : PlanBuilder.Removal.Goal<Belief, Goal, Env, Context>
    }

    sealed interface FailureInterception<Belief : Any, Goal : Any, Env : Environment> :
        TriggerBuilder<Belief, Goal, Env>{
        fun <Context : Any> goal(goalQuery: Goal.() -> Context?)
                : PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context>
    }
}

@JaktaDSL
sealed interface PlanBuilder<Belief : Any, Goal: Any, Env : Environment, Context : Any> {

    sealed interface Addition<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
        PlanBuilder<Belief, Goal, Env, Context> {

        interface Belief<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
            Addition<Belief, Goal, Env, Context> {

            infix fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?):
                    PlanBuilder.Addition.Belief<Belief, Goal, Env, Context>

            fun <PlanResult> triggers(
                body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
                resultType: KType
            ): Plan.Belief.Addition<Belief, Goal, Env, Context, PlanResult>
        }

        interface Goal<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
            Addition<Belief, Goal, Env, Context> {
            infix fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?):
                    PlanBuilder.Addition.Goal<Belief, Goal, Env, Context>

            fun <PlanResult> triggers(
                body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
                resultType: KType
            ): Plan.Goal.Addition<Belief, Goal, Env, Context, PlanResult>
        }

    }

    sealed interface Removal<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
        PlanBuilder<Belief, Goal, Env, Context> {

        interface Belief<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
            Removal<Belief, Goal, Env, Context> {
            infix fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?):
                    PlanBuilder.Removal.Belief<Belief, Goal, Env, Context>

            fun <PlanResult> triggers(
                body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
                resultType: KType
            ): Plan.Belief.Removal<Belief, Goal, Env, Context, PlanResult>
        }

        interface Goal<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
            Removal<Belief, Goal, Env, Context> {
            infix fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?):
                    PlanBuilder.Removal.Goal<Belief, Goal, Env, Context>

            fun <PlanResult> triggers(
                body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
                resultType: KType
            ): Plan.Goal.Removal<Belief, Goal, Env, Context, PlanResult>
        }
    }

    sealed interface FailureInterception<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
     PlanBuilder<Belief, Goal, Env, Context> {

        interface Goal<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
            FailureInterception<Belief, Goal, Env, Context> {
            infix fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?):
                    PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context>

            fun <PlanResult> triggers(
                body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
                resultType: KType
            ): Plan.Goal.Failure<Belief, Goal, Env, Context, PlanResult>
        }
    }
}


inline infix fun <Belief : Any, Goal : Any, Env : Environment, Context : Any, reified PlanResult>
PlanBuilder.Addition.Belief<Belief, Goal, Env, Context>.triggers(
    noinline body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult
): Plan.Belief.Addition<Belief, Goal, Env, Context, PlanResult> {
    return this.triggers(body, typeOf<PlanResult>())
}

inline infix fun <Belief : Any, Goal : Any, Env : Environment, Context : Any, reified PlanResult>
        PlanBuilder.Addition.Goal<Belief, Goal, Env, Context>.triggers(
    noinline body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult
): Plan.Goal.Addition<Belief, Goal, Env, Context, PlanResult> {
    return this.triggers(body, typeOf<PlanResult>())
}

inline infix fun <Belief : Any, Goal : Any, Env : Environment, Context : Any, reified PlanResult>
PlanBuilder.Removal.Belief<Belief, Goal, Env, Context>.triggers(
    noinline body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult
): Plan.Belief.Removal<Belief, Goal, Env, Context, PlanResult> {
    return this.triggers(body, typeOf<PlanResult>())
}

inline infix fun <Belief : Any, Goal : Any, Env : Environment, Context : Any, reified PlanResult>
        PlanBuilder.Removal.Goal<Belief, Goal, Env, Context>.triggers(
    noinline body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult
): Plan.Goal.Removal<Belief, Goal, Env, Context, PlanResult> {
    return this.triggers(body, typeOf<PlanResult>())
}

inline infix fun <Belief : Any, Goal : Any, Env : Environment, Context : Any, reified PlanResult>
        PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context>.triggers(
    noinline body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult
): Plan.Goal.Failure<Belief, Goal, Env, Context, PlanResult> {
    return this.triggers(body, typeOf<PlanResult>())
}




