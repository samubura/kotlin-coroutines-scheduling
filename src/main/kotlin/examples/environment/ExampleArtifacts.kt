package examples.environment

import environment.Artifact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.sync.withLock
import log
import kotlin.random.Random


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

    private suspend fun tick() {
        val currentTime = getProperty<Int>("time")
        updateProperty("time", currentTime + 1)
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

class FakeFetchArtifact : Artifact() {
    var scope : CoroutineScope? = null;

    override suspend fun init(scope: CoroutineScope) {this.scope = scope}

    //TODO This is WRONG! An artifact operation should never block an agent!
    suspend fun get() : Int = coroutineScope {
        log("I'm an artifact doing some background work")
        Thread.sleep(1000) // FAKE WORK
        log("My work is not done yet")
        Thread.sleep(1000) //FAKE WORK
        log("Done!")
        42
    }

    //TODO This is somewhat wrong! Although not blocking the agent, an artifact operation should run
    // within the artifact scope
    // but here it is running in the agent scope, the agent will suspend and resume
    // i.e. the artifact operation is not "atomic"
    suspend fun getSuspend() : Int {
        log("I'm an artifact doing some background work")
        delay(1000) // FAKE WORK
        log("My work is not done yet")
        delay(1000) //FAKE WORK
        log("Done!")
        return 42
    }

    //TODO This is correct, the agent suspends but does not block, and the artifact runs in the background
    // using the main MAS scope to run the async operation
    suspend fun getAsync() : Int {
        val value = scope?.async {
            log("I'm an artifact doing some background work")
            Thread.sleep(1000) // FAKE WORK
            log("My work is not done yet")
            delay(2000)
            log("Done!")
            42
        }

        return value!!.await()
    }

}
