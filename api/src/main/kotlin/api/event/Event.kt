package api.event

import api.intention.Intention
import api.intention.IntentionID
import api.plan.Plan
import api.query.Query
import kotlinx.coroutines.CompletableDeferred

sealed interface Event {
    sealed interface Internal : Event {
        val intention: Intention?

        sealed interface Goal<PlanResult : Any> : Internal {
            val completion: CompletableDeferred<PlanResult>

            sealed interface Test<TQ: Query.Test, PlanResult : Any> : Goal<PlanResult> {
                val query: TQ

                interface Add<TQ: Query.Test, PlanResult : Any> : Test<TQ, PlanResult>

                interface Remove<TQ: Query.Test, PlanResult : Any> : Test<TQ, PlanResult>
            }

            sealed interface Achieve<GoalType : Any, PlanResult : Any> : Goal<PlanResult> {
                val goal: GoalType

                interface Add<GoalType : Any, PlanResult : Any> : Achieve<GoalType, PlanResult>

                interface Remove<GoalType : Any, PlanResult : Any> : Achieve <GoalType, PlanResult>
            }
        }

        sealed interface Belief<out B : Any> : Internal {
            val belief: B

            interface Add<out B : Any> : Belief<B>

            interface Remove<out B : Any> : Belief<B>
        }

        data class Step(override val intention: Intention) : Internal
    }

    interface External : Event
}