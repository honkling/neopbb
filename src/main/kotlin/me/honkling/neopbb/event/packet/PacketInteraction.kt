package me.honkling.neopbb.event.packet

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.parser.InteractionType
import me.honkling.commando.common.parser.handle.FunctionHandle
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod

class PacketInteraction : InteractionType<Nothing?, Nothing?>(), PacketListener {
    private val annotations = listOf(ServerboundPacket::class, ClientboundPacket::class)
    private val listeners = mutableListOf<Node<Nothing?>>()

    override fun testParent(parent: KClass<*>): Result<Nothing?> {
        for (method in parent.java.declaredMethods) {
            println("Checking class ${method.name}")
            val annotation = method.annotations.find { it.annotationClass in annotations }
            println("Annotation: $annotation")

            if (annotation == null)
                continue

            val event = if (annotation is ClientboundPacket) PacketSendEvent::class.java
                        else PacketReceiveEvent::class.java
            println("Found event: $event")
            println("${method.parameterCount}, ${method.parameterTypes[0]}")

            if (method.parameterCount != 1 || method.parameterTypes[0] != event)
                continue

            println("Success!")
            return Result.success(null)
        }

        return Result.failure(IllegalStateException("Class has no listener methods"))
    }

    override fun testFunction(parent: KClass<*>, handle: FunctionHandle): Result<Nothing?> {
        val reflector = handle.reflector
        val annotation = reflector.javaMethod!!.annotations.find { it.annotationClass in annotations }

        if (annotation == null)
            return Result.failure(IllegalArgumentException("Function doesn't have annotation"))

        val event = if (annotation is ClientboundPacket) PacketSendEvent::class
                    else PacketReceiveEvent::class

        if (reflector.parameters.size != 1)
            return Result.failure(IllegalArgumentException("Function has too little or too many parameters"))

        if (reflector.parameters.first().type != event.createType())
            return Result.failure(IllegalArgumentException("Function doesn't have correct parameter type"))

        return Result.success(null)
    }

    override fun createRootNode(parent: KClass<*>): Node<Nothing?> {
        return Node(null, parent.java.name, null)
    }

    override fun parse(root: Node<Nothing?>, parent: KClass<*>, handle: FunctionHandle) {
        root.children += Node(root, handle.name, handle.reflector)
    }

    override fun execute(root: Node<Nothing?>, context: Nothing?): Result<Nothing?> {
        return Result.success(null)
    }

    override fun postParse(root: Node<Nothing?>) {
        listeners += root
    }

    override fun onPacketSend(event: PacketSendEvent) {
        val functions = listeners.map { it.children }
            .flatten()
            .map { it.context as KFunction<*> }

        for (function in functions) {
            val annotation = function.javaMethod!!.annotations.find { it.annotationClass == ClientboundPacket::class } as? ClientboundPacket
                ?: continue

            if (event.packetType == annotation.type) {
                function.isAccessible = true
                function.call(event)
            }
        }
    }

    override fun onPacketReceive(event: PacketReceiveEvent) {
        val functions = listeners.map { it.children }
            .flatten()
            .map { it.context as KFunction<*> }

        for (function in functions) {
            val annotation = function.javaMethod!!.annotations.find { it.annotationClass == ServerboundPacket::class } as? ServerboundPacket
                ?: continue

            if (event.packetType == annotation.type) {
                function.isAccessible = true
                function.call(event)
            }
        }
    }
}