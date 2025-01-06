package me.honkling.prisonbutbad.feature

import me.honkling.commando.common.node.Node
import me.honkling.commando.common.parser.InteractionType
import me.honkling.commando.common.parser.handle.FunctionHandle
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.isAccessible

val features = mutableMapOf<String, Int>()

class FeatureInteraction : InteractionType<Nothing?, Nothing?>() {
    override fun testParent(parent: KClass<*>): Result<Nothing?> {
        println("Has annotation: ${parent.hasAnnotation<Feature>()}")
        if (parent.java.annotations.none { it.annotationClass == Feature::class })
            return Result.failure(IllegalStateException("Class doesn't have @Feature"))

        println("Success")
        val methods = parent.java.declaredMethods
        val schedule = methods.find { it.name == "schedule" }
        val execute = methods.find { it.name == "execute" }
        if (schedule == null || execute == null)
            return Result.failure(IllegalStateException("Class doesn't have schedule & execute"))

        if (schedule.parameterCount != 0 || schedule.returnType != Int::class.java)
            return Result.failure(IllegalStateException("Schedule is invalid"))

        if (execute.parameterCount != 0)
            return Result.failure(IllegalStateException("Execute is invalid"))

        println("Good")
        return Result.success(null)
    }

    override fun testFunction(parent: KClass<*>, handle: FunctionHandle): Result<Nothing?> {
        val reflector = handle.reflector
        val parameters = reflector.parameters
        println("!!! Testing ${handle.name}")
        if (handle.name == "schedule")
            println("Schedule: $parameters ${reflector.returnType}")
        val isSuccess = when (handle.name) {
            "schedule" -> parameters.isEmpty()
                    && (reflector.returnType == Int::class.createType()
                     || reflector.returnType == java.lang.Integer::class.createType())
            "execute" -> parameters.isEmpty()
            else -> return Result.failure(IllegalArgumentException("Not correctly named"))
        }

        if (!isSuccess)
            return Result.failure(IllegalArgumentException("Function has parameters"))

        return Result.success(null)
    }

    override fun createRootNode(parent: KClass<*>): Node<Nothing?> {
        val annotation = parent.java.annotations.find { it.annotationClass == Feature::class }!! as Feature
        return Node(null, annotation.name, null)
    }

    override fun parse(root: Node<Nothing?>, parent: KClass<*>, handle: FunctionHandle) {
        println("Parsing function!!! '${handle.name}'")
        root.children += Node(root, handle.name, handle.reflector)
    }

    override fun execute(root: Node<Nothing?>, context: Nothing?): Result<Nothing?> {
        @Suppress("UNCHECKED_CAST")
        val schedule = root.children.find { it.name == "schedule" }!!.context as KFunction<Int>
        schedule.isAccessible = true
        val taskId = schedule.call()
        features[root.name] = taskId
        return Result.success(null)
    }

    override fun postParse(root: Node<Nothing?>) {
        execute(root, null)
    }
}