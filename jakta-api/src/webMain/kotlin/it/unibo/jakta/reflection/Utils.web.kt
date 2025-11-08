package it.unibo.jakta.reflection

import kotlin.reflect.KType

//TODO check if a better subtype check is possible
actual fun KType.isSubtypeOfMultiPlatform(other: KType): Boolean = this == other
