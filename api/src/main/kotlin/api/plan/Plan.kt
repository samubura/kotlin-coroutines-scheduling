package api.plan

import api.query.Query

interface Plan<Trigger: PlanTrigger, Guard : Query.Test, PlanResult : Any> {
    val id: PlanID
    val trigger: Trigger
    val guard: Guard

    //TODO Does it make sense to have these generic? (Env and PlanResult)
    // Or are these only useful when defining a plan, but not in practice?
    // i.e. an agent will have different plans that have any type of result...
    // but will have plans that all have the same (sub-)type of environment???
    //val body : suspend PlanScope<Env>() -> PlanResult

    val body: suspend () -> PlanResult
}
