package me.honkling.neopbb.config

import cc.ekblad.toml.decode
import cc.ekblad.toml.encodeToString
import cc.ekblad.toml.tomlMapper
import me.honkling.neopbb.config.lib.locationDecoder
import me.honkling.neopbb.config.lib.use
import me.honkling.neopbb.config.lib.useLocationEncoder
import me.honkling.neopbb.currentPrison
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.switchMap
import org.bukkit.Bukkit
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
        val prisonerCells: List<Location>,
        val blackMarketIn: Location,
        val blackMarketOut: Location,
        val respawn: Location,
        val bertrude: Location,
        val solitaryCells: List<Location>
    )
}

const val prisonFileName = "prisons.toml"

fun reloadPrisonsToml() {
    val file = instance.dataFolder.resolve(prisonFileName)
    val mapper = tomlMapper {
        use(locationDecoder)
    }

    if (!file.exists()) {
        instance.dataFolder.mkdirs()
        instance.saveResource(file.name, true)
    }

    prisonsToml = mapper.decode(file.toPath())
}

// Regex for world, x,y,z,yaw,pitch for pretty text :)
val locationBlockRegex = Regex("""(\w+)\.world\s*=\s*([^\n]+)\s*
\1\.x\s*=\s*([^\n]+)\s*
\1\.y\s*=\s*([^\n]+)\s*
\1\.z\s*=\s*([^\n]+)\s*
\1\.yaw\s*=\s*([^\n]+)\s*
\1\.pitch\s*=\s*([^\n]+)""".trimIndent(), RegexOption.MULTILINE)


fun savePrisonsToml() {
    val file = instance.dataFolder.resolve(prisonFileName)
    val mapper = tomlMapper {
        use(locationDecoder)
        useLocationEncoder()
    }

    var tomlText = mapper.encodeToString(prisonsToml)

    // prettying up the text before writing to disk ;l.
    tomlText = tomlText.replace(locationBlockRegex) { match ->
        val property = match.groupValues[1]
        val world = match.groupValues[2]
        val x = match.groupValues[3]
        val y = match.groupValues[4]
        val z = match.groupValues[5]
        val yaw = match.groupValues[6]
        val pitch = match.groupValues[7]

        "$property = { world = $world, x = $x, y = $y, z = $z, yaw = $yaw, pitch = $pitch }"
    }

    file.writeText(tomlText)
}

/**
 * Goose's has it as private, so we are going to respect that and just make this handle all changes...
 *
 * This function is use for all changing a prison outside of this kt.
 */
fun updatePrison(prisonName: String, newPrison: PrisonsToml.Prison?) {
    // This is for removing a prison.
    if (newPrison == null) {
        val updatedList = prisonsToml.prisons.filterNot { it.name.equals(prisonName, ignoreCase = true) }
        prisonsToml = prisonsToml.copy(prisons = updatedList)
        savePrisonsToml()

        if (currentPrison.name.equals(prisonName, ignoreCase = true)) {
            currentPrison = updatedList.firstOrNull() ?: return
            switchMap(currentPrison)
            Bukkit.broadcast("<p>The current prison named $prisonName was removed.".mm)
        }
        return
    }

    // This is for creating a prison.
    if (prisonsToml.prisons.none { it.name.equals(newPrison.name, ignoreCase = true) }) {
        prisonsToml = prisonsToml.copy(prisons = prisonsToml.prisons + newPrison)
    // This is for editing prison.
    } else {
        val updatedList = prisonsToml.prisons.map { if (it.name.equals(prisonName, ignoreCase = true)) newPrison else it }
        prisonsToml = prisonsToml.copy(prisons = updatedList)
    }

    savePrisonsToml()

    if (currentPrison.name.equals(prisonName, ignoreCase = true))
        currentPrison = newPrison
}
