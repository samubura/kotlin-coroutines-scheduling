package api.intention

import api.intention.Intention
import api.intention.MutableIntentionPool
import api.intention.MutableIntentionPoolImpl
import kotlinx.coroutines.Job
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class IntentionPoolTest {

    private lateinit var intentionPool: MutableIntentionPool
    private val intention: Intention = Intention(job = Job())
    private val updatedIntention = Intention(id = intention.id, job = Job()) // Note: Same ID as intention

    @BeforeEach
    fun init() {
        intentionPool = MutableIntentionPoolImpl()
    }

    @Test
    fun testInsertion() {
        assert(intentionPool.tryPut(intention)) {
            "Inserting an intention inside an empty intention pool has failed"
        }
        assert(intentionPool.getIntentionsSet().contains(intention)) {
            "The tryPut() operation was successful, but the intention is not in the pool"
        }
        assert(intentionPool.getIntentionsSet().size == 1)
    }

    @Test
    fun testIntentionsEquivalence() {
        assert(intention.id == updatedIntention.id) {
            "IntentionID equivalence is not working"
        }
        assert(intention == updatedIntention) {
            "The two intentions should be considered equals since their ID is the same"
        }
    }

    @Test
    fun testIntentionUpdate() {
        testInsertion()
        assert(intentionPool.getIntentionsSet().contains(updatedIntention)) {
            "The Intention Pool should recognise updatedIntention as already present in the pool"
        }
        assert(intentionPool.tryPut(updatedIntention))
        val intentions = intentionPool.getIntentionsSet()
        assert(intentions.size == 1)

        // Verifying that the intention is the same but the body is updated correctly in the pool
        assert(intentions.first() == updatedIntention)
        assert(intentions.first().job == updatedIntention.job)
        assert(intentions.first().job != intention.job)
    }
}