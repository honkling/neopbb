package me.honkling.neopbb.lib

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

fun isClassPrimitive(found: KClass<*>, expected: KClass<*>): Boolean {
    return found == expected || found.java == expected.javaPrimitiveType
}

fun isClassPrimitive(found: KType, expected: KClass<*>): Boolean {
    val expectedType = expected.createType()
    val primitiveType = expected.javaPrimitiveType!!.kotlin.createType()
    return found == expectedType || found == primitiveType
}