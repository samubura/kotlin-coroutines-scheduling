package api.intention

import api.event.GoalAddEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals

class IntentionPoolTest {
    private lateinit var intentionPool: MutableIntentionPool
    private lateinit var agentJob: Job
    private lateinit var intentionJob: Job
    private lateinit var otherJob: Job
    private lateinit var intention: Intention
    private lateinit var otherIntention: Intention

    private lateinit var newGoalEvent: GoalAddEvent<String, Unit>

    @BeforeEach
    fun init() {
        intentionPool = MutableIntentionPoolImpl(Channel())
        agentJob = SupervisorJob()
        intentionJob = Job(agentJob)
        otherJob = Job(agentJob)
        intention = Intention(job = intentionJob)
        otherIntention = Intention(job = otherJob)
        newGoalEvent =
            GoalAddEvent(
                goal = "NewGoal",
                resultType = typeOf<Unit>(),
                completion = CompletableDeferred(),
                intention = null,
            )
    }

    @Test
    fun testInsertion() {
        assert(intentionPool.tryPut(intention)) {
            "Inserting an intention inside an empty intention pool has failed"
        }
        assert(intentionPool.getIntentionsSet().contains(intention)) {
            "The tryPut() operation was successful, but the intention is not in the pool"
        }
        assert(intentionPool.getIntentionsSet().size == 1) {
            "The intention pool should contain exactly one intention"
        }
    }

    @Test
    fun testDoubleInsertion() {
        testInsertion()
        assertFails { testInsertion() }
    }

    @Test
    fun testDropIntention() {
        testInsertion()
        runTest {
            assert(intentionPool.drop(intention.id)) {
                "It should be possible to drop an existing intention from the pool"
            }
        }
        assert(!intentionPool.getIntentionsSet().contains(intention)) {
            "The intention should no longer be present in the pool after being dropped"
        }
        assert(intentionPool.getIntentionsSet().isEmpty()) {
            "The intention pool should be empty after dropping the only intention it contained"
        }
        assert(intention.job.isCancelled) {
            "The job associated to the dropped intention should be cancelled"
        }
        assert(otherIntention.job.isCancelled.not()) {
            "The job associated to an intention that has not been dropped should not be cancelled"
        }
        assert(agentJob.isCancelled.not()) {
            "The job associated to the agent should not be cancelled when dropping an intention"
        }
    }

    @Test
    fun testNextIntentionWithNewGoal() {
        runTest {
            launch(agentJob) {
                assertEquals(
                    agentJob,
                    currentCoroutineContext().job.parent,
                    "The coroutine context job's parent should be the agent job",
                )

                val nextIntention = intentionPool.nextIntention(newGoalEvent)
                assertContains(
                    agentJob.children,
                    nextIntention.job.parent,
                    "The intention job's parent should be a children of the agent job",
                )

                val otherNext = intentionPool.nextIntention(newGoalEvent)
                assertContains(
                    agentJob.children,
                    otherNext.job.parent,
                    "The other intention job's parent should be a children of the agent job",
                )
                assertNotEquals(
                    nextIntention.job,
                    otherNext.job,
                    "Two different intentions should not have the same job",
                )
                assertEquals(
                    nextIntention.job.parent,
                    otherNext.job.parent,
                    "But they should have the same parent job",
                )
            }
        }
    }

    @Test
    fun testNextIntentionWithExistingIntention() {
        testInsertion()
        val subGoalEvent = newGoalEvent.copy(intention = intention)
        runTest {
            launch(agentJob) {
                val nextIntention =
                    intentionPool.nextIntention(
                        subGoalEvent,
                    )
                assertEquals(
                    intention,
                    nextIntention,
                    "The intention returned by nextIntention() should be the same as the one referenced by the event",
                )
                assertEquals(
                    intention.job,
                    nextIntention.job,
                    "The job of the intention returned by nextIntention() should be the same as the one referenced by the event",
                )
            }
        }
    }

    @Test
    fun testCancellingIntentions() {
        runTest {
            launch(agentJob) {
                val nextIntention = intentionPool.nextIntention(newGoalEvent)
                val otherNext = intentionPool.nextIntention(newGoalEvent)
                val subGoalIntention = intentionPool.nextIntention(newGoalEvent.copy(intention = nextIntention))

                agentJob.cancelAndJoin()

                assert(nextIntention.job.isCancelled) {
                    "The intention job should be cancelled after the agent job is cancelled"
                }
                assert(subGoalIntention.job.isCancelled) {
                    "The subgoal job should be cancelled after the agent job is cancelled"
                }
                assert(otherNext.job.isCancelled) {
                    "The other intention job should be cancelled after the agent job is cancelled"
                }
                assert(intentionPool.getIntentionsSet().isEmpty()) {
                    "Intentions are removed"
                }
            }
        }
    }
}
