package it.unibo.jakta.agent

import it.unibo.jakta.JaktaDSL

@JaktaDSL
interface GoalBuilder<Goal : Any> {
    operator fun Goal.not()
}

class GoalBuilderImpl<Goal : Any>(val addGoal: (Goal) -> Unit) : GoalBuilder<Goal> {
    override operator fun Goal.not() {
        addGoal(this)
    }
}
