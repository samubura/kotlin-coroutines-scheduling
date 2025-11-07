package api.environment

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.random.Random

class TestEnvironment(val seed: Int = 1234) : Environment {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun currentTime(): Long =
        currentCoroutineContext()[TestCoroutineScheduler]?.currentTime ?:
            super.currentTime()

    override suspend fun nextRandom(): Int = Random(seed).nextInt()


    fun test() {
        // Just a test function to illustrate usage
    }
}
