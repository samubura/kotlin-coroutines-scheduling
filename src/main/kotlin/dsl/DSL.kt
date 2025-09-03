package dsl

import kotlinx.coroutines.CoroutineScope
import query.Query
import kotlin.coroutines.CoroutineContext

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

@JaktaDSL
sealed interface PlanContext

interface GuardContext : PlanContext

interface FinalPlanContext : PlanContext, CoroutineScope {
    val agent: Any
}

@JaktaDSL
sealed interface Trigger<QueryResult> {
    sealed interface OnBelief<Belief : Any, QueryResult,> : Trigger<QueryResult> {
        fun <Context: PlanContext, PlanResult> belief(belief: Query.Belief<Belief, QueryResult>): PlanConstructor<Context, PlanResult>
    }
    sealed interface OnGoal<QueryResult> : Trigger<QueryResult> {
        fun <Context: PlanContext, PlanResult> goal(goal: Query.Goal<QueryResult>): PlanConstructor<Context, PlanResult>
    }
}
sealed interface Addition<Belief : Any, QueryResult> : Trigger.OnBelief<Belief, QueryResult>, Trigger.OnGoal<QueryResult>
sealed interface Removal<Belief : Any, QueryResult> : Trigger.OnBelief<Belief, QueryResult>, Trigger.OnGoal<QueryResult>
sealed interface FailureInterception<QueryResult> : Trigger.OnGoal<QueryResult>

@JaktaDSL
interface Agent<Belief: Any, QueryResult> {
    val adding: Addition<Belief, QueryResult> get() = TODO()
    val removing: Removal<Belief, QueryResult> get() = TODO()
    val failing: FailureInterception<QueryResult> get() = TODO()
}



@JaktaDSL
interface PlanConstructor<Context: PlanContext, PlanResult> {
    val context: Context
}

suspend infix fun <Context: FinalPlanContext, PlanResult> PlanConstructor<Context, PlanResult>.triggers(body: Context.() -> PlanResult): PlanResult = TODO()

infix fun <InputContext: GuardContext, OutputContext: FinalPlanContext, PlanResult> PlanConstructor<InputContext, PlanResult>
        .onlyIf(predicate: InputContext.() -> OutputContext): PlanConstructor<OutputContext, PlanResult> = TODO()

@JaktaDSL
fun <Belief, QueryResult> agent(
    block: Agent<Belief, QueryResult>.() -> Unit
): Agent<Belief, QueryResult> where Belief: Any = TODO()

@JvmInline
value class RegexQuery(val regex: Regex) : Query.Belief<Regex, MatchResult?> {
    constructor(regex: String) : this(Regex(regex))
}

@JaktaDSL
class RegexPlanContext(val matches: List<MatchResult>) : GuardContext
class RegexPlanContextWithAgent(val matches: List<MatchResult>) : FinalPlanContext

@DslMarker
annotation class JaktaDSL

fun main() {
    agent {
        adding.belief(RegexQuery("pluto")) onlyIf {
            RegexPlanContextWithAgent(emptyList())
        } triggers {
            agent.toString()
            matches.all { it.groups.isNotEmpty() }
        }
    }
}