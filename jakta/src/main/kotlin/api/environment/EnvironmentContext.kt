package api.environment

import kotlin.coroutines.CoroutineContext

class EnvironmentContext(val environment: Environment) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<EnvironmentContext>
}
