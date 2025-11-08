package it.unibo.jakta.intention

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import kotlinx.coroutines.Job

class IntentionTest : ShouldSpec({

    context("Intentions with the same id") {
        val id = IntentionID()
        val i1 = Intention(id=id, job = Job())
        val i2 = Intention(id=id, job = Job())

        should("be equal even if their jobs are different") {
            i1 shouldBeEqual i2
        }
    }

    context("Intentions with different ids") {
        val i1 = Intention(job = Job())
        val i2 = Intention(job = Job())

        should("not be equal") {
            i1 shouldNotBeEqual i2
        }
    }
})
