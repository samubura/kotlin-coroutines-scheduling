fun <T> String.ifGoalMatch(goal: String, returnValue: T): T? {
    return if (this == goal) returnValue else null
}

fun String.ifGoalMatch(goal: String): Unit? {
    return if (this == goal) Unit else null
}