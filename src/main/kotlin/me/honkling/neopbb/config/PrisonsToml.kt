package me.honkling.neopbb.config

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import me.honkling.neopbb.config.lib.location
import me.honkling.neopbb.config.lib.use
import me.honkling.neopbb.instance
import org.bukkit.Location
import org.bukkit.Material

lateinit var prisonsToml: PrisonsToml; private set

data class PrisonsToml(
    val prisons: List<Prison>
) {
    data class Prison(
        val name: String,
        val icon: Material,
        val wardenSpawn: Location,
        val cells: List<Location>,
        val blackMarketIn: Location,
        val blackMarketOut: Location,
        val respawn: Location,
        val bertrude: Location,
        val solitary: Location
    )
}


fun reloadPrisonsToml() {
    val file = instance.dataFolder.resolve("prisons.toml")
    val mapper = tomlMapper {
        use(location)
    }

    if (!file.exists()) {
        instance.dataFolder.mkdirs()
        instance.saveResource(file.name, true)
    }

    prisonsToml = mapper.decode(file.toPath())
}