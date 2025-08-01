package environment

import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext

abstract class Environment () {
    val mutex:  Mutex = Mutex()
}

class EnvironmentContext(val environment: Environment) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key
    companion object Key : CoroutineContext.Key<EnvironmentContext>
}
