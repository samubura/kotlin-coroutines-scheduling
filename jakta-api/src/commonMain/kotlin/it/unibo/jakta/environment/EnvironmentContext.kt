package it.unibo.jakta.environment

import kotlin.coroutines.CoroutineContext

class EnvironmentContext(val environment: it.unibo.jakta.environment.Environment) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<it.unibo.jakta.environment.EnvironmentContext>
}
