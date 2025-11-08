package it.unibo.jakta.environment

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


interface Environment {

    @OptIn(ExperimentalTime::class)
    suspend fun currentTime() : Long = Clock.System.now().toEpochMilliseconds()

    suspend fun nextRandom() : Double = Random.nextDouble()

    fun getRandomizer(seed: Int) : Random = Random(seed)

}
