package me.honkling.neopbb.lib

import me.honkling.commonlib.lib.CaseType
import me.honkling.commonlib.lib.convertCase
import me.honkling.neopbb.config.itemTypeKey
import me.honkling.neopbb.config.itemsToml
import me.honkling.neopbb.gui.recipeKey
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KProperty

class ItemProvider {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): ItemStack {
        val key = property.name.convertCase(CaseType.Snake)

        return itemsToml.items[key]
            ?: throw IllegalStateException("Missing '$key' item in items.toml")
    }
}

private val items = ItemProvider()

val key by items

fun ItemStack.displayName(component: Component): ItemStack {
    editMeta {
        it.displayName(component)
    }

    return this
}

fun ItemStack.lore(vararg components: Component): ItemStack {
    editMeta {
        it.lore(components.toList())
    }

    return this
}

val ItemStack.itemType: String?
    get() = itemMeta?.persistentDataContainer?.get(itemTypeKey, PersistentDataType.STRING)

val ItemStack.recipe: List<ItemStack>?
    get() = itemMeta?.persistentDataContainer?.get(recipeKey, PersistentDataType.LIST.strings())
        ?.map { itemsToml.items[it]!! }

val shopPriceKey = NamespacedKey(namespace, "shop_price")
val ItemStack.shopPrice: Double?
    get() = itemMeta?.persistentDataContainer?.get(shopPriceKey, PersistentDataType.DOUBLE)