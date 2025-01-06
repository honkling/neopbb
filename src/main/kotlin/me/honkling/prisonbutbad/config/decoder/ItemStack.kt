package me.honkling.prisonbutbad.config.decoder

import cc.ekblad.toml.model.TomlValue
import me.honkling.commonlib.config.decoder.Decoder
import me.honkling.prisonbutbad.lib.mm
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

val itemStack: Decoder = ItemStack::class to { type, value ->
    if (value is TomlValue.Map) {
        val properties = value.properties
        val material = Material.matchMaterial((properties["type"]!! as TomlValue.String).value)!!
        val itemStack = ItemStack.of(material)
        itemStack.editMeta {
            it.displayName((properties["name"]!! as TomlValue.String).value.mm)
        }
        itemStack
    } else value
}