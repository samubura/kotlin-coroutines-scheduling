package agent

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch



suspend fun main(): kotlin.Unit = coroutineScope{
    val bob = Agent("Bob")
    val alice = Agent("Alice")

    launch { bob.run() }
    launch { alice.run() }



}