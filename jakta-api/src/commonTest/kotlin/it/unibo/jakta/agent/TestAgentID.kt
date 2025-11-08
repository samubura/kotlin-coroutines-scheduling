package it.unibo.jakta.agent

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class TestAgentID : ShouldSpec({

    context("two agentIDs with the same id but different names") {
        val id = "test-id"
        val agent1 = AgentID(name = "Agent One", id = id)
        val agent2 = AgentID(name = "Agent Two", id = id)

        should("be equal") {
            agent1 shouldBe agent2
        }

        should("have the same hash code") {
            agent1.hashCode() shouldBe agent2.hashCode()
        }

        should("display the correct names") {
            agent1.displayName shouldBe "Agent One"
            agent2.displayName shouldBe "Agent Two"
        }
    }

    context("two agentIDs with the same name but different ids") {
        val testName = "AgentName"
        val agent1 = AgentID(name = testName)
        val agent2 = AgentID(name = testName)

        should("not be equal") {
            agent1 shouldNotBe agent2
        }

        should("not have the same hash code") {
            agent1.hashCode() shouldNotBe agent2.hashCode()
        }

        should("display the correct names") {
            agent1.displayName shouldBe testName
            agent2.displayName shouldBe testName
        }
    }


    context("an agentID with no name") {
        val testId = "test-id"
        val agent = AgentID(id = testId)

        should("display the id as name") {
            agent.displayName shouldBe "Agent-$testId"
        }
    }
})


