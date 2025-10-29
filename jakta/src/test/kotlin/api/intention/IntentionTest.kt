package api.intention

import kotlinx.coroutines.Job
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IntentionTest {

    private lateinit var intention: Intention


    @BeforeEach
    fun init() {
        intention = Intention(job = Job())
    }

    @Test
    fun testIntentionsEquivalence() {
        assert(intention == Intention(id = intention.id, job = Job())){
            "The two intentions should be considered equals since their ID is the same, even if their job is different"
        }
        assert(intention != Intention(job = Job())) {
            "The two intentions should be considered different since their ID is different"
        }
    }


}