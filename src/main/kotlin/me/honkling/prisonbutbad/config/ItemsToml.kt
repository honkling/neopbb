package me.honkling.prisonbutbad.config

import cc.ekblad.toml.tomlMapper
import me.honkling.commonlib.config.decoder.use
import me.honkling.commonlib.config.getAndMapConfig
import me.honkling.prisonbutbad.config.decoder.itemStack
import me.honkling.prisonbutbad.instance
import org.bukkit.inventory.ItemStack

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
    return itemsToml
}