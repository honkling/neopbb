package me.honkling.neopbb.config.lib

import cc.ekblad.toml.configuration.TomlMapperConfigurator
import cc.ekblad.toml.model.TomlValue
import cc.ekblad.toml.util.InternalAPI
import org.bukkit.Bukkit
import org.bukkit.Location
import kotlin.reflect.KType

val locationDecoder: Decoder = Location::class to { type: KType, it: TomlValue ->
    if (it !is TomlValue.Map)
        it
    else {
        val (properties) = it

        if (!properties.keys.containsAll(listOf("x", "y", "z", "world")))
            it
        else
            Location(
                Bukkit.getWorld(properties["world"]!!.value<String>()),
                properties["x"]!!.value(),
                properties["y"]!!.value(),
                properties["z"]!!.value(),
                properties["yaw"]?.value<Double>()?.toFloat() ?: 0f,
                properties["pitch"]?.value<Double>()?.toFloat() ?: 0f
            )
    }
}

fun TomlMapperConfigurator.useLocationEncoder() {
    @OptIn(InternalAPI::class)
    this.encoder(Location::class) { value ->
        val loc = value as Location
        TomlValue.Map(
            mapOf(
                "world" to TomlValue.String(loc.world?.name ?: "world"),
                "x" to TomlValue.Double(loc.x),
                "y" to TomlValue.Double(loc.y),
                "z" to TomlValue.Double(loc.z),
                "yaw" to TomlValue.Double(loc.yaw.toDouble()),
                "pitch" to TomlValue.Double(loc.pitch.toDouble())
            )
        )
    }
}

