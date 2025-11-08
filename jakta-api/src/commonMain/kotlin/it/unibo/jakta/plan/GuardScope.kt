package it.unibo.jakta.plan

interface GuardScope<Belief : Any> {
    val beliefs: Collection<Belief>
}
