package it.unibo.jakta.intention

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.CoroutineContext.Key

sealed interface Intention : CoroutineContext.Element {
    val id: it.unibo.jakta.intention.IntentionID

    /**
     * Parent of all the chain of sub-executions of continuations.
     * If one child exits, the failure can be detected by the parent.
     * If parent dies, all children die too.
     */
    val job: Job

    /**
     * The channel for executing intentions, allows for round-robin fair execution of intentions.
     * New intentions added in the channel are executed on a first-come-first-served basis,
     * however it does NOT allow for setting up priorities.
     */
    val continuations: Channel<() -> Unit>

    /**
     * Executes one step of the intention, i.e. executes its body until the next suspension.
     */
    fun step(): Unit

    fun onReadyToStep(callback: (it.unibo.jakta.intention.Intention) -> Unit): Unit

    fun enqueue(continuation: () -> Unit): Unit

    companion object Key : CoroutineContext.Key<it.unibo.jakta.intention.Intention> {
        operator fun invoke(
            id: it.unibo.jakta.intention.IntentionID = _root_ide_package_.it.unibo.jakta.intention.IntentionID(),
            job: Job,
            continuations: Channel<() -> Unit> = Channel(Channel.UNLIMITED),
        ): it.unibo.jakta.intention.Intention =
            _root_ide_package_.it.unibo.jakta.intention.IntentionImpl(id, job, continuations)
    }
}

internal data class IntentionImpl(
    override val id: it.unibo.jakta.intention.IntentionID = _root_ide_package_.it.unibo.jakta.intention.IntentionID(),
    override val job: Job,
    override val continuations: Channel<() -> Unit> = Channel(Channel.UNLIMITED),
) : it.unibo.jakta.intention.Intention {
    private val log =
        Logger(
            Logger.config,
            "Intention[${id.id}]",
        )

    override val key: Key<it.unibo.jakta.intention.Intention> = _root_ide_package_.it.unibo.jakta.intention.Intention.Key

    val observers: MutableList<(it.unibo.jakta.intention.Intention) -> Unit> = mutableListOf()

    override fun equals(other: Any?): Boolean = (other is it.unibo.jakta.intention.Intention && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun step() {
        continuations.tryReceive().getOrNull()?.let {
            log.d { "Running one step" }
            it()
        }
    }

    override fun onReadyToStep(callback: (it.unibo.jakta.intention.Intention) -> Unit) {
        observers.add(callback)
    }

    private fun notifyReadyToStep() {
        observers.forEach { it(this) }
    }

    override fun enqueue(continuation: () -> Unit) {
        log.d { "Resumed continuation and notify ready to step" }
        continuations.trySend(continuation)
        notifyReadyToStep()
    }
}
