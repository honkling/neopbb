package me.honkling.prisonbutbad.lib

import kotlin.reflect.KClass

fun isClassPrimitive(found: KClass<*>, expected: KClass<*>): Boolean {
    return found == expected || found.java == expected.javaPrimitiveType
}