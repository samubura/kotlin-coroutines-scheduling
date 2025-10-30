package api.plan

import dsl.JaktaDSL

@JaktaDSL
interface GuardScope<Belief : Any> {
    val beliefs: Collection<Belief>
}
