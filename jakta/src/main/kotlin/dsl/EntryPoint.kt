package dsl

import api.agent.Agent
import api.environment.Environment
import api.mas.MAS
import api.plan.Plan
import dsl.agent.AgentBuilder
import dsl.agent.AgentBuilderImpl
import dsl.mas.MasBuilder
import dsl.mas.MasBuilderImpl
import dsl.plan.PlanBuilder
import dsl.plan.TriggerAdditionImpl
import dsl.plan.TriggerRemovalImpl


@JaktaDSL
fun <Belief : Any, Goal : Any, Env : Environment> mas(
    block: MasBuilder<Belief, Goal, Env>.() -> Unit
): MAS<Belief, Goal, Env> {
    val mb = MasBuilderImpl<Belief, Goal, Env>()
    mb.apply(block)
    return mb.build()
}

@JaktaDSL
fun <Belief : Any, Goal : Any, Env : Environment> agent(block: AgentBuilder<Belief, Goal, Env>.() -> Unit): Agent<Belief, Goal, Env> {
    val ab = AgentBuilderImpl<Belief, Goal, Env>()
    ab.apply(block)
    return ab.build()
}

//TODO entrypoint for plans???
// this is tricky due to the way the DSL is constructed
// create an entrypoint for a single standalone plan is hard...


//TODO this works but it is error prone.
// a user might be tempted to create multiple belief plans in the same block but only the latter is returned


// TODO maybe actually make the triggerBuilder implement these interfaces?
interface BeliefOnlyAdditionTrigger<Belief : Any, Goal : Any, Env: Environment> {
    fun <Context : Any> belief(beliefQuery: Belief.() -> Context?)
            : PlanBuilder.Addition.Belief<Belief, Goal, Env, Context>
}

interface BeliefOnlyRemovalTrigger<Belief : Any, Goal : Any, Env: Environment> {
    fun <Context : Any> belief(beliefQuery: Belief.() -> Context?)
            : PlanBuilder.Removal.Belief<Belief, Goal, Env, Context>
}

class BeliefPlan<Belief: Any, Goal: Any, Env: Environment>{

    val adding: BeliefOnlyAdditionTrigger<Belief, Goal, Env>
        get() = object : BeliefOnlyAdditionTrigger<Belief, Goal, Env> {
            val trigger = TriggerAdditionImpl<Belief, Goal, Env>({}, {})
            override fun <Context : Any> belief(beliefQuery: Belief.() -> Context?): PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> {
                return trigger.belief(beliefQuery)
            }
        }

    val removing: BeliefOnlyRemovalTrigger<Belief, Goal, Env>
        get() = object : BeliefOnlyRemovalTrigger<Belief, Goal, Env> {
            val trigger = TriggerRemovalImpl<Belief, Goal, Env>({}, {})
            override fun <Context : Any> belief(beliefQuery: Belief.() -> Context?): PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> {
                return trigger.belief(beliefQuery)
            }
        }

    companion object {
        fun < Belief: Any, Goal: Any, Env: Environment> of(
            block: BeliefPlan<Belief, Goal, Env>.() -> Plan.Belief<Belief, Goal, Env, *, *>
        ) : Plan.Belief<Belief, Goal, Env, *, *> {
            return block(BeliefPlan())
        }
    }
}


