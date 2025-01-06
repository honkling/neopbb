package me.honkling.prisonbutbad.lib

import me.honkling.commonlib.lib.CaseType
import me.honkling.commonlib.lib.convertCase
import me.honkling.prisonbutbad.config.itemsToml
import org.bukkit.inventory.ItemStack
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
