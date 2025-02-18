package me.honkling.neopbb.config.decoder

import cc.ekblad.toml.model.TomlValue
import me.honkling.commonlib.config.decoder.Decoder
import me.honkling.neopbb.gui.recipeKey
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.lib.shopPriceKey
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

val itemStack: Decoder = ItemStack::class to { _, value ->
    if (value is TomlValue.Map) {
        val properties = value.properties
        val material = Material.matchMaterial((properties["type"]!! as TomlValue.String).value)!!
        val itemStack = ItemStack.of(material)
        itemStack.editMeta { meta ->
            (properties["name"] as? TomlValue.String)?.value?.mm
                ?.let { meta.displayName(it) }

            val price = (properties["price"] as? TomlValue.Double)?.value
            val container = meta.persistentDataContainer

            if (price != null)
                container.set(shopPriceKey, PersistentDataType.DOUBLE, price)

            val craftRecipe = (properties["recipe"] as? TomlValue.List)?.elements

            if (craftRecipe?.any { it !is TomlValue.String } == true || craftRecipe?.isNotEmpty() != true)
                return@editMeta

            @Suppress("UNCHECKED_CAST")
            craftRecipe as List<TomlValue.String>?

            container.set(recipeKey, PersistentDataType.LIST.strings(), craftRecipe.map(TomlValue.String::value))
        }

        itemStack
    } else value
}