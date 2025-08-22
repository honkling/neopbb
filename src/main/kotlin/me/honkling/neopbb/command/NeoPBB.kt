@file:Command("neopbb", permission = "neopbb.admin")

package me.honkling.neopbb.command

import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.config.*
import me.honkling.neopbb.lib.mm
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private fun reloadConfig(sender: CommandSender, config: String) {
    when (config) {
        "main" -> {
            reloadConfigToml()
            sender.sendMessage("<p>Reloaded the main configuration.".mm)
        }
        "filter" -> {
            reloadFilterToml()
            sender.sendMessage("<p>Reloaded the filter configuration.".mm)
        }
        "prisons" -> {
            reloadPrisonsToml()
            sender.sendMessage("<p>Reloaded the prisons configuration.".mm)
        }
        else -> {
            sender.sendMessage("<p>Unknown configuration.".mm)
        }
    }
}

private fun `reloadConfig$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return listOf("filter", "prisons", "main").filter { it.contains(input, true) }
}

// === Builder Dangerous Commands ===

private fun createPrison(sender: Player, prisonName: String) {
    if (prisonsToml.prisons.any { it.name.equals(prisonName, true) })
        return sender.sendMessage("<p>The prison named <s>$prisonName</s> already exists.".mm)

    val defaultLocation = sender.location
    val newPrison = PrisonsToml.Prison(
        name = prisonName,
        icon = Material.STONE,
        wardenSpawn = defaultLocation,
        respawn = defaultLocation,
        bertrude = defaultLocation,
        blackMarketIn = defaultLocation,
        blackMarketOut = defaultLocation,
        prisonerCells = mutableListOf(),
        solitaryCells = mutableListOf()
    )

    updatePrison(prisonName, newPrison)
    savePrisonsToml()
    sender.sendMessage("<p>The <s>$prisonName</s> prison has been created.".mm)
}

private fun removePrison(sender: Player, prison: PrisonsToml.Prison) {
    updatePrison(prison.name, null)
    savePrisonsToml()

    sender.sendMessage("<p><s>${prison.name}</s> has been removed.".mm)
}
