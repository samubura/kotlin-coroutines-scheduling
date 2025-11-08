package it.unibo.jakta.agent

import kotlin.reflect.typeOf

@Suppress("DEPRECATION_ERROR")
suspend inline fun <B : Any, G : Any, reified PlanResult> AgentActions<B, G>.achieve(goal: G): PlanResult =
    internalAchieve(goal, typeOf<PlanResult>())

// TODO(Ambiguous :((((( as expected. I'd like to avoid to specify the return type of achieve when this is Unit)
// @Suppress("DEPRECATION_ERROR")
// suspend fun <B: Any, G: Any> AgentActions<B, G>.achieve(goal: G) : Unit =
//    _achieve(goal, typeOf<Unit>())

