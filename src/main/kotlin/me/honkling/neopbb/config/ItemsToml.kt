package me.honkling.neopbb.config

import cc.ekblad.toml.tomlMapper
import me.honkling.commonlib.config.decoder.use
import me.honkling.commonlib.config.getAndMapConfig
import me.honkling.neopbb.config.decoder.itemStack
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.namespace
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

val itemTypeKey = NamespacedKey(namespace, "item_type")
var itemsToml = loadItemsToml(); private set

data class ItemsToml(
    val items: Map<String, ItemStack>
)

fun loadItemsToml(): ItemsToml {
    val mapper = tomlMapper {
        use(itemStack)
    }

    instance.saveResource("items.toml", true)
    itemsToml = getAndMapConfig<ItemsToml>("items.toml", mapper)

    for ((id, item) in itemsToml.items)
        item.editMeta {
            it.persistentDataContainer.set(itemTypeKey, PersistentDataType.STRING, id)
        }

    return itemsToml
}