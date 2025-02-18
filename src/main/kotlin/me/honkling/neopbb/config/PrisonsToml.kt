package me.honkling.neopbb.config

import cc.ekblad.toml.tomlMapper
import me.honkling.commonlib.config.decoder.location
import me.honkling.commonlib.config.decoder.use
import me.honkling.commonlib.config.getAndMapConfig
import me.honkling.neopbb.config.decoder.world
import me.honkling.neopbb.instance
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

var prisonsToml = loadPrisonsToml(); private set
val fallbackPrison: PrisonsToml.Prison
    get() {
        val fallback = prisonsToml.prisons["fallback"]

        if (fallback == null) {
            val world = Bukkit.getWorlds()[0]

            return PrisonsToml.Prison(
                "Fallback",
                world,
                Location(world, 0.0, 0.0, 0.0),
                Location(world, 0.0, 0.0, 0.0)
            )
        }

        return fallback
    }

data class PrisonsToml(
    val prisons: Map<String, Prison>
) {
    data class Prison(
        val name: String,
        val world: World,
        val blackMarketEnter: Location,
        val blackMarketExit: Location
    )
}

fun loadPrisonsToml(): PrisonsToml {
    val mapper = tomlMapper {
        mapping<PrisonsToml.Prison>(
            "black-market-enter" to "blackMarketEnter",
            "black-market-exit" to "blackMarketExit"
        )

        use(world)
        use(location)
    }

    instance.saveResource("prisons.toml", true)
    prisonsToml = getAndMapConfig<PrisonsToml>("prisons.toml", mapper)
    return prisonsToml
}