package api.environment

import kotlin.random.Random

interface Environment {

    suspend fun currentTime() : Long = System.currentTimeMillis()

    suspend fun nextRandom() : Int = Random.nextInt()
}
