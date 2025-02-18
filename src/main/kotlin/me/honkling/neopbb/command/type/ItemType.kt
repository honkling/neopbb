package me.honkling.neopbb.command.type

import me.honkling.commando.common.node.Node
import me.honkling.commando.common.platform.User
import me.honkling.commando.common.type.Type
import me.honkling.commonlib.lib.CaseType
import me.honkling.commonlib.lib.convertCase
import me.honkling.neopbb.lib.key
import org.bukkit.inventory.ItemStack
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.kotlinFunction

class ItemType : Type<ItemStack>() {
    private val items: Map<String, () -> ItemStack>

    init {
        val properties = ::key.javaGetter!!.declaringClass.declaredMethods.mapNotNull {
            if (!it.name.startsWith("get") || !it.name.endsWith("\$delegate"))
                return@mapNotNull null

            val name = it.name
                .removePrefix("get")
                .removeSuffix("\$delegate")
                .convertCase(CaseType.Snake)

            if (it.returnType != ItemStack::class.java) null
            else name to { -> it.invoke(null) as ItemStack }
        }

        items = mapOf(*properties.toTypedArray())
    }

    override fun parse(
        user: User<*>,
        node: Node<*>,
        input: String,
        autoCompleting: Boolean
    ): Result<Pair<ItemStack, String>> {
        val key = input(input, 1).lowercase()
        val getter = items[key]
            ?: return Result.failure(IllegalArgumentException("Expected a valid item key, found '$key'"))

        return Result.success(getter() to input.substringAfter(' ', ""))
    }

    override fun suggest(user: User<*>, node: Node<*>, input: String): List<String> {
        val first = input(input, 1)
        return items.map { it.key }
            .filter { first in it }
    }
}