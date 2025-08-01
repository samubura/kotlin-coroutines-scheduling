package examples.environment

import environment.Artifact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock


class Clock : Artifact() {

    override suspend fun init(scope: CoroutineScope) {
        mutex.withLock{
            defineProperty("time", 0)
        }

        scope.launch {
            while (true){
                delay(1000)
                tick()
            }
        }
    }

    suspend fun tick() {
        mutex.withLock {
            val currentTime = getProperty<Int>("time")
            updateProperty("time", currentTime + 1)
        }
    }

    suspend fun reset() {
        mutex.withLock {
            updateProperty("time", 0)
        }
    }

    suspend fun getTime(): Int {
        mutex.withLock {
            return getProperty("time")
        }
    }
}