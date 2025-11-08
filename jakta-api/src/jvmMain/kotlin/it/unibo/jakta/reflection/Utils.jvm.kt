package it.unibo.jakta.reflection

import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf

actual fun KType.isSubtypeOfMultiPlatform(other: KType): Boolean = this.isSubtypeOf(other)
