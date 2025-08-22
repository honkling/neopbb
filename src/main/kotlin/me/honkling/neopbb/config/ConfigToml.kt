package me.honkling.neopbb.config

import cc.ekblad.toml.decode
import cc.ekblad.toml.model.TomlValue
import cc.ekblad.toml.tomlMapper
import dev.kord.common.entity.Snowflake
import me.honkling.neopbb.config.lib.use
import me.honkling.neopbb.instance

lateinit var configToml: ConfigToml; private set

data class ConfigToml(
    val discord: Discord
) {
    data class Discord(
        val invite: String,
        val token: String,
        val channelId: Snowflake,
        val reportsId: Snowflake,
        val staffLogsId: Snowflake,
        val staffId: Snowflake
    )
}

fun reloadConfigToml() {
    val file = instance.dataFolder.resolve("config.toml")
    val mapper = tomlMapper {
        use(Snowflake::class to { _, value ->
            if (value !is TomlValue.Integer)
                value
            else Snowflake(value.value)
        })
    }

    if (!file.exists()) {
        instance.dataFolder.mkdirs()
        instance.saveResource(file.name, true)
    }

    configToml = mapper.decode(file.toPath())
}