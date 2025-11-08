package it.unibo.jakta

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentBuilder
import it.unibo.jakta.agent.AgentBuilderImpl
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.mas.MAS
import it.unibo.jakta.mas.MasBuilder
import it.unibo.jakta.mas.MasBuilderImpl
import it.unibo.jakta.plan.Plan
import it.unibo.jakta.plan.PlanBuilder
import it.unibo.jakta.plan.TriggerAdditionImpl
import it.unibo.jakta.plan.TriggerRemovalImpl

@JaktaDSL
fun <Belief : Any, Goal : Any, Env : Environment> mas(
    block: MasBuilder<Belief, Goal, Env>.() -> Unit,
): MAS<Belief, Goal, Env> {
    val mb = MasBuilderImpl<Belief, Goal, Env>()
    mb.apply(block)
    return mb.build()
}

@JaktaDSL
fun <Belief : Any, Goal : Any, Env : Environment> agent(
    block: AgentBuilder<Belief, Goal, Env>.() -> Unit,
): Agent<Belief, Goal, Env> {
    val ab = AgentBuilderImpl<Belief, Goal, Env>()
    ab.apply(block)
    return ab.build()
}

// TODO entrypoint for plans???
// this is tricky due to the way the DSL is constructed
// create an entrypoint for a single standalone plan is hard...

// TODO this works but it is error prone.
// a user might be tempted to create multiple belief plans in the same block but only the latter is returned

// TODO maybe actually make the triggerBuilder implement these interfaces?
interface BeliefOnlyAdditionTrigger<Belief : Any, Goal : Any, Env : Environment> {
    fun <Context : Any> belief(
        beliefQuery: Belief.() -> Context?,
    ): PlanBuilder.Addition.Belief<Belief, Goal, Env, Context>
}

interface BeliefOnlyRemovalTrigger<Belief : Any, Goal : Any, Env : Environment> {
    fun <Context : Any> belief(
        beliefQuery: Belief.() -> Context?,
    ): PlanBuilder.Removal.Belief<Belief, Goal, Env, Context>
}

class BeliefPlan<Belief : Any, Goal : Any, Env : Environment> {
    val adding: BeliefOnlyAdditionTrigger<Belief, Goal, Env>
        get() =
            object : BeliefOnlyAdditionTrigger<Belief, Goal, Env> {
                val trigger = TriggerAdditionImpl<Belief, Goal, Env>({}, {})

                override fun <Context : Any> belief(
                    beliefQuery: Belief.() -> Context?,
                ): PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> = trigger.belief(beliefQuery)
            }

    val removing: BeliefOnlyRemovalTrigger<Belief, Goal, Env>
        get() =
            object : BeliefOnlyRemovalTrigger<Belief, Goal, Env> {
                val trigger = TriggerRemovalImpl<Belief, Goal, Env>({}, {})

                override fun <Context : Any> belief(
                    beliefQuery: Belief.() -> Context?,
                ): PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> = trigger.belief(beliefQuery)
            }

    companion object {
        fun <Belief : Any, Goal : Any, Env : Environment> of(
            block: BeliefPlan<Belief, Goal, Env>.() -> Plan.Belief<Belief, Goal, Env, *, *>,
        ): Plan.Belief<Belief, Goal, Env, *, *> = block(BeliefPlan())
    }
}
