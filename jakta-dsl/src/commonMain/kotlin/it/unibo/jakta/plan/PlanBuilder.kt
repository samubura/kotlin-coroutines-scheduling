package it.unibo.jakta.plan

import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.Plan
import it.unibo.jakta.plan.PlanScope
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@JaktaDSL
sealed interface PlanBuilder<B : Any, G : Any, Env : Environment, Context : Any> {
    sealed interface Addition<B : Any, G : Any, Env : Environment, Context : Any> : PlanBuilder<B, G, Env, Context> {
        interface Belief<B : Any, G : Any, Env : Environment, Context : Any> : Addition<B, G, Env, Context> {
            infix fun onlyWhen(guard: GuardScope<B>.(Context) -> Context?): Belief<B, G, Env, Context>

            @Deprecated("Use triggers instead", ReplaceWith("triggers(body)"), DeprecationLevel.ERROR)
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
            ): Plan.Belief.Addition<B, G, Env, Context, PlanResult>
        }

        interface Goal<B : Any, G : Any, Env : Environment, Context : Any> : Addition<B, G, Env, Context> {
            infix fun onlyWhen(guard: GuardScope<B>.(Context) -> Context?): Goal<B, G, Env, Context>

            @Deprecated("Use triggers instead", ReplaceWith("triggers(body)"), DeprecationLevel.ERROR)
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
            ): Plan.Goal.Addition<B, G, Env, Context, PlanResult>
        }
    }

    sealed interface Removal<B : Any, G : Any, Env : Environment, Context : Any> : PlanBuilder<B, G, Env, Context> {
        interface Belief<B : Any, G : Any, Env : Environment, Context : Any> : Removal<B, G, Env, Context> {
            infix fun onlyWhen(guard: GuardScope<B>.(Context) -> Context?): Belief<B, G, Env, Context>

            @Deprecated("Use triggers instead", ReplaceWith("triggers(body)"), DeprecationLevel.ERROR)
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
            ): Plan.Belief.Removal<B, G, Env, Context, PlanResult>
        }

        interface Goal<B : Any, G : Any, Env : Environment, Context : Any> : Removal<B, G, Env, Context> {
            infix fun onlyWhen(guard: GuardScope<B>.(Context) -> Context?): Goal<B, G, Env, Context>

            @Deprecated("Use triggers instead", ReplaceWith("triggers(body)"), DeprecationLevel.ERROR)
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
            ): Plan.Goal.Removal<B, G, Env, Context, PlanResult>
        }
    }

    sealed interface FailureInterception<B : Any, G : Any, Env : Environment, Context : Any> :
        PlanBuilder<B, G, Env, Context> {
        // TODO should we add Belief failure interception??

        interface Goal<B : Any, G : Any, Env : Environment, Context : Any> :
            FailureInterception<B, G, Env, Context> {
            infix fun onlyWhen(guard: GuardScope<B>.(Context) -> Context?): Goal<B, G, Env, Context>

            @Deprecated("Use triggers instead", ReplaceWith("triggers(body)"), DeprecationLevel.ERROR)
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
            ): Plan.Goal.Failure<B, G, Env, Context, PlanResult>
        }
    }
}

@Suppress("DEPRECATION_ERROR")
inline infix fun <B, G, Env, Context, reified PlanResult> PlanBuilder.Addition.Belief<B, G, Env, Context>.triggers(
    noinline body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
): Plan.Belief.Addition<B, G, Env, Context, PlanResult> where B : Any, G : Any, Env : Environment, Context : Any =
    this.triggersImpl(typeOf<PlanResult>(), body)

@Suppress("DEPRECATION_ERROR")
inline infix fun <B : Any, G : Any, Env : Environment, Context : Any, reified PlanResult> PlanBuilder.Addition.Goal<B, G, Env, Context>.triggers(
    noinline body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
): Plan.Goal.Addition<B, G, Env, Context, PlanResult> =
    this.triggersImpl(typeOf<PlanResult>(), body)

@Suppress("DEPRECATION_ERROR")
inline infix fun <B : Any, G : Any, Env : Environment, Context : Any, reified PlanResult> PlanBuilder.Removal.Belief<B, G, Env, Context>.triggers(
    noinline body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
): Plan.Belief.Removal<B, G, Env, Context, PlanResult> =
    this.triggersImpl(typeOf<PlanResult>(), body)

@Suppress("DEPRECATION_ERROR")
inline infix fun <B : Any, G : Any, Env : Environment, Context : Any, reified PlanResult> PlanBuilder.Removal.Goal<B, G, Env, Context>.triggers(
    noinline body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
): Plan.Goal.Removal<B, G, Env, Context, PlanResult> = this.triggersImpl(typeOf<PlanResult>(), body)

@Suppress("DEPRECATION_ERROR")
inline infix fun <B : Any, G : Any, Env : Environment, Context : Any, reified PlanResult> PlanBuilder.FailureInterception.Goal<B, G, Env, Context>.triggers(
    noinline body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
): Plan.Goal.Failure<B, G, Env, Context, PlanResult> = this.triggersImpl(typeOf<PlanResult>(), body)
