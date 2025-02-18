package me.honkling.neopbb.config.decoder

import cc.ekblad.toml.model.TomlValue
import me.honkling.commonlib.config.decoder.Decoder
import org.bukkit.Bukkit
import org.bukkit.World

val world: Decoder = World::class to { _, value ->
    if (value is TomlValue.String) {
        val stringValue = value.value
        val world = Bukkit.getWorld(stringValue)
        world ?: value
    } else value
}