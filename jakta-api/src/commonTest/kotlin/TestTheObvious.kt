import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class TestTheObvious : ShouldSpec({
    should("always pass") {
        true shouldBe true
    }
})
