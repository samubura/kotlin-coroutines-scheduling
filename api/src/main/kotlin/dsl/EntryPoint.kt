package dsl

import api.environment.Environment
import api.mas.MAS
import dsl.mas.MasBuilder
import dsl.mas.MasBuilderImpl

@JaktaDSL
fun <Belief : Any, Goal : Any, Env : Environment> mas(
    block: MasBuilder<Belief, Goal, Env>.() -> Unit
): MAS<Belief, Goal, Env> {
    val mb = MasBuilderImpl<Belief, Goal, Env>()
    mb.apply(block)
    return mb.build()
}

//TODO I want more entrypoints, to be able to define stuff in multiple files..
// this should be easy, we can do it later


