package dsl

//////////////////////////////////////////////////////////////////////
// DSL
//////////////////////////////////////////////////////////////////////

@DslMarker
annotation class JaktaDSL

@JaktaDSL
interface MasBuilder<Belief : Any, Goal : Any, Env : Environment> {

    @JaktaDSL
    fun agent(
        block: AgentBuilder<Belief, Goal, Env>.() -> Unit
    ): Agent<Belief, Goal, Env>

    fun environment (block: () -> Env)
}

@JaktaDSL
interface AgentBuilder<Belief : Any, Goal : Any, Env: Environment> {

    fun believes(
        block: BeliefBuilder<Belief>.() -> Unit
    ) : Collection<Belief>

    fun hasInitialGoals(
        block: GoalBuilder<Goal>.() -> Unit
    ) : Sequence<Goal>

    fun hasPlans(
        block: PlanLibraryBuilder<Belief, Goal, Env>.() -> Unit
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
interface PlanLibraryBuilder<Belief : Any, Goal : Any, Env: Environment> {
    val adding: TriggerBuilder.Addition<Belief, Goal, Env>
    val removing: TriggerBuilder.Removal<Belief, Goal, Env,>
    val failing: TriggerBuilder.FailureInterception<Belief, Goal, Env>
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
                : PlanBuilder.Removal.Goal<Belief, Goal, Env, Context>
    }
}

@JaktaDSL
sealed interface PlanBuilder<Belief : Any, Goal: Any, Env : Environment, Context : Any> {

    sealed interface Addition<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
        PlanBuilder<Belief, Goal, Env, Context> {

        interface Belief<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
            Addition<Belief, Goal, Env, Context> {

            infix fun <OutputContext : Any> onlyWhen(guard: GuardScope<Belief>.(Context) -> OutputContext?):
                    PlanBuilder.Addition.Belief<Belief, Goal, Env, OutputContext>

            infix fun <PlanResult> triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult):
                    Plan.Addition.Belief<Belief, Goal, Env, Context, PlanResult>
        }

        interface Goal<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
            Addition<Belief, Goal, Env, Context> {
            infix fun <OutputContext : Any> onlyWhen(guard: GuardScope<Belief>.(Context) -> OutputContext?):
                    PlanBuilder.Addition.Goal<Belief, Goal, Env, OutputContext>

            infix fun <PlanResult> triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult):
                    Plan.Addition.Goal<Belief, Goal, Env, Context, PlanResult>
        }

    }

    sealed interface Removal<Belief : Any, Goal : Any, Env : Environment, Context : Any> {
        interface Belief<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
            Removal<Belief, Goal, Env, Context> {
            infix fun <OutputContext : Any> onlyWhen(guard: GuardScope<Belief>.(Context) -> OutputContext?):
                    PlanBuilder.Removal.Belief<Belief, Goal, Env, OutputContext>

            infix fun <PlanResult> triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult):
                    Plan.Removal.Belief<Belief, Goal, Env, Context, PlanResult>
        }

        interface Goal<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
            Removal<Belief, Goal, Env, Context> {
            infix fun <OutputContext : Any> onlyWhen(guard: GuardScope<Belief>.(Context) -> OutputContext?):
                    PlanBuilder.Removal.Goal<Belief, Goal, Env, OutputContext>

            infix fun <PlanResult> triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult):
                    Plan.Removal.Goal<Belief, Goal, Env, Context, PlanResult>
        }
    }

    sealed interface FailureInterception<Belief : Any, Goal : Any, Env : Environment, Context : Any> {

        interface Goal<Belief : Any, Goal : Any, Env : Environment, Context : Any> :
            FailureInterception<Belief, Goal, Env, Context> {
            infix fun <OutputContext : Any> onlyWhen(guard: GuardScope<Belief>.(Context) -> OutputContext?):
                    PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, OutputContext>

            infix fun <PlanResult> triggers(body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult):
                    Plan.GoalFailure<Belief, Goal, Env, Context, PlanResult>
        }
    }
}

