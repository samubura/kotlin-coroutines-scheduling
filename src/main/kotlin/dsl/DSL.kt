package dsl

//////////////////////////////////////////////////////////////////////
// DSL
//////////////////////////////////////////////////////////////////////

@DslMarker
annotation class JaktaDSL

@JaktaDSL
sealed interface TriggerBuilder<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> {

    sealed interface Addition<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> :
        TriggerBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>{

        fun <PlanResult> belief(beliefQuery: Belief.() -> BeliefQueryResult?)
                : PlanBuilder.Addition.Belief<Belief, Goal, Env, BeliefQueryResult, PlanResult>

        fun <PlanResult> goal(goalQuery: Goal.() -> GoalQueryResult?)
                : PlanBuilder.Addition.Goal<Belief, Goal, Env, GoalQueryResult, PlanResult>
    }

    sealed interface Removal<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> :
        TriggerBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>{

        fun <PlanResult> belief(beliefQuery: Belief.() -> BeliefQueryResult?)
                : PlanBuilder.Removal.Belief<Belief, Goal, Env, BeliefQueryResult, PlanResult>

        fun <PlanResult> goal(goalQuery: Goal.() -> GoalQueryResult?)
                : PlanBuilder.Removal.Goal<Belief, Goal, Env, GoalQueryResult, PlanResult>
    }

    sealed interface FailureInterception<Belief : Any, Goal : Any, Env : Environment, BeliefQueryResult : Any, GoalQueryResult : Any> :
        TriggerBuilder<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>{
        fun <PlanResult> goal(goalQuery: Goal.() -> GoalQueryResult?)
                : PlanBuilder.Removal.Goal<Belief, Goal, Env, GoalQueryResult, PlanResult>
    }
}


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
    ): Sequence<Plan<Belief, Goal, Env, Any, Any, Any>>  // TODO FIX GENERICS
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
    val adding: TriggerBuilder.Addition<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>
    val removing: TriggerBuilder.Removal<Belief, Goal, Env,  BeliefQueryResult, GoalQueryResult>
    val failing: TriggerBuilder.FailureInterception<Belief, Goal, Env, BeliefQueryResult, GoalQueryResult>
}

@JaktaDSL
sealed interface PlanBuilder<Belief : Any, Goal: Any, Env : Environment, Context : Any, PlanResult> {

    sealed interface Addition<Belief : Any, Goal : Any, Env : Environment, Context : Any, PlanResult> :
        PlanBuilder<Belief, Goal, Env, Context, PlanResult> {

        interface Belief<Belief : Any, Goal : Any, Env : Environment, Context : Any, PlanResult> :
            Addition<Belief, Goal, Env, Context, PlanResult> {
            infix fun <OutputContext : Context> onlyWhen(guard: GuardScope<Belief>.(Context) -> OutputContext?): //TODO Weird behavior
                    PlanBuilder.Addition.Belief<Belief, Goal, Env, OutputContext, PlanResult>

            infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult):
                    Plan.Addition.Belief<Belief, Goal, Env, Context, PlanResult>
        }

        interface Goal<Belief : Any, Goal : Any, Env : Environment, Context : Any, PlanResult> :
            Addition<Belief, Goal, Env, Context, PlanResult> {
            infix fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?):
                    PlanBuilder.Addition.Goal<Belief, Goal, Env, Context, PlanResult>

            infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult):
                    Plan.Addition.Goal<Belief, Goal, Env, Context, PlanResult>
        }

    }

    sealed interface Removal<Belief : Any, Goal : Any, Env : Environment, Context : Any, PlanResult> {
        interface Belief<Belief : Any, Goal : Any, Env : Environment, Context : Any, PlanResult> :
            Removal<Belief, Goal, Env, Context, PlanResult> {
            infix fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?):
                    PlanBuilder.Removal.Belief<Belief, Goal, Env, Context, PlanResult>

            infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult):
                    Plan.Removal.Belief<Belief, Goal, Env, Context, PlanResult>
        }

        interface Goal<Belief : Any, Goal : Any, Env : Environment, Context : Any, PlanResult> :
            Removal<Belief, Goal, Env, Context, PlanResult> {
            infix fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?):
                    PlanBuilder.Removal.Goal<Belief, Goal, Env, Context, PlanResult>

            infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult):
                    Plan.Removal.Goal<Belief, Goal, Env, Context, PlanResult>
        }
    }

    sealed interface FailureInterception<Belief : Any, Goal : Any, Env : Environment, Context : Any, PlanResult> {

        interface Goal<Belief : Any, Goal : Any, Env : Environment, Context : Any, PlanResult> :
            FailureInterception<Belief, Goal, Env, Context, PlanResult> {
            infix fun onlyWhen(guard: GuardScope<Belief>.(Context) -> Context?):
                    PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context, PlanResult>

            infix fun triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult):
                    Plan.GoalFailure<Belief, Goal, Env, Context, PlanResult>
        }
    }
}
