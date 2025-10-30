package marmellata

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

class TestInterceptor {
    object CustomInterceptor : ContinuationInterceptor {
        override val key: CoroutineContext.Key<*>
            get() = ContinuationInterceptor.Key

        override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> =
            object : Continuation<T> {
                override val context: CoroutineContext = continuation.context

                override fun resumeWith(result: Result<T>) {
                    println("Intercepted continuation with context: $context")
                    continuation.resumeWith(result)
                }
            }
    }

    @Test
    fun testMinimal() {
        runTest {
            val job =
                launch(CustomInterceptor) {
                    println("Hello...")
                    delay(3000)
                    println("World!")
                }
            job.join()
        }
    }
}
