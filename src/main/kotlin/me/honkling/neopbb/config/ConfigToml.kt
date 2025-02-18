package me.honkling.neopbb.config

import cc.ekblad.toml.tomlMapper
import me.honkling.commonlib.config.getAndMapConfig
import me.honkling.neopbb.instance

var configToml = loadConfigToml(); private set

data class ConfigToml(
    val warden: Warden
) {
    data class Warden(
        val invulnerability: Float,
        val defaultCooldown: Float,
        val resignCooldown: Float
    )
}

fun loadConfigToml(): ConfigToml {
    val mapper = tomlMapper {
        mapping<ConfigToml.Warden>(
            "default-cooldown" to "defaultCooldown",
            "resign-cooldown" to "resignCooldown"
        )
    }

    instance.saveResource("config.toml", true)
    configToml = getAndMapConfig<ConfigToml>("config.toml", mapper)
    return configToml
}