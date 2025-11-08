package it.unibo.jakta.reflection

import kotlin.reflect.KType

expect fun KType.isSubtypeOfMultiPlatform(other: KType): Boolean
