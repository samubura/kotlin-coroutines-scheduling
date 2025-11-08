package it.unibo.jakta.intention

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.sequences.shouldContain
import it.unibo.jakta.event.GoalAddEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlin.reflect.typeOf

class IntentionPoolTest : ShouldSpec({

    lateinit var intentionPool: MutableIntentionPool
    lateinit var agentJob: Job
    lateinit var intentionJob: Job
    lateinit var otherJob: Job
    lateinit var intention: Intention
    lateinit var otherIntention: Intention
    lateinit var newGoalEvent: GoalAddEvent<String, Unit>

    beforeEach {
        intentionPool = MutableIntentionPoolImpl(Channel())
        agentJob = SupervisorJob()
        intentionJob = Job(agentJob)
        otherJob = Job(agentJob)
        intention = Intention(job = intentionJob)
        otherIntention = Intention(job = otherJob)
        newGoalEvent = GoalAddEvent(
            goal = "NewGoal",
            resultType = typeOf<Unit>(),
            completion = CompletableDeferred(),
            intention = null,
        )
    }

    should("allow to insert an intention into an empty pool") {
        intentionPool.tryPut(intention).shouldBeTrue()
        intentionPool.getIntentionsSet().shouldContain(intention)
        intentionPool.getIntentionsSet().size shouldBe 1
    }

    should("fail when inserting the same intention twice") {
        intentionPool.tryPut(intention).shouldBeTrue()
        intentionPool.tryPut(intention).shouldBeFalse()
        intentionPool.getIntentionsSet().size shouldBe 1
    }

    should("drop an existing intention") {
        intentionPool.tryPut(intention).shouldBeTrue()

        runTest {
            intentionPool.drop(intention.id).shouldBeTrue()
        }

        intentionPool.getIntentionsSet().shouldBeEmpty()
        intention.job.isCancelled.shouldBeTrue()
        otherIntention.job.isCancelled.shouldBeFalse()
        agentJob.isCancelled.shouldBeFalse()
    }

    should("create new intention when an event has no reference") {
        runTest {
            launch(agentJob) {
                val next = intentionPool.nextIntention(newGoalEvent)
                agentJob.children.shouldContain(next.job.parent)

                val otherNext = intentionPool.nextIntention(newGoalEvent)
                agentJob.children.shouldContain(otherNext.job.parent)

                next.job shouldNotBe otherNext.job
                next.job.parent shouldBe otherNext.job.parent
            }
        }
    }

    should("reuse an existing intention when an event references it") {
        intentionPool.tryPut(intention)

        val event = newGoalEvent.copy(intention = intention)

        runTest {
            launch(agentJob) {
                val next = intentionPool.nextIntention(event)
                next shouldBe intention
                next.job shouldBe intention.job
                intentionPool.getIntentionsSet().size shouldBe 1
            }
        }
    }

    should("cancel all intentions when the agent job is cancelled") {
        runTest {
            launch(agentJob) {
                val i1 = intentionPool.nextIntention(newGoalEvent)
                val i2 = intentionPool.nextIntention(newGoalEvent)
                val sub = intentionPool.nextIntention(newGoalEvent.copy(intention = i1))

                agentJob.cancelAndJoin()

                i1.job.isCancelled.shouldBeTrue()
                i2.job.isCancelled.shouldBeTrue()
                sub.job.isCancelled.shouldBeTrue()
                intentionPool.getIntentionsSet().shouldBeEmpty()
            }
        }
    }
})
