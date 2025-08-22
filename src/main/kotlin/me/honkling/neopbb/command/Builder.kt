@file:Command("builder", permission = "neopbb.builder")

package me.honkling.neopbb.command

import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.config.PrisonsToml.Prison
import me.honkling.neopbb.config.prisonsToml
import me.honkling.neopbb.config.savePrisonsToml
import me.honkling.neopbb.config.updatePrison
import me.honkling.neopbb.lib.CellType
import me.honkling.neopbb.lib.getOrdinal
import me.honkling.neopbb.lib.isInCell
import me.honkling.neopbb.lib.mm
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private fun setIcon(sender: Player, prison: Prison, icon: Material) {
    val newPrison = prison.copy(icon = icon)

    updatePrison(prison.name, newPrison)
    savePrisonsToml()

    val iconDisplay = icon.name[0] + icon.name.substring(1).lowercase()
    sender.sendMessage("<p><s>${prison.name}</s>'s icon has been changed to <s>$iconDisplay</s>.".mm)
}


fun addCell(sender: Player, prison: Prison, cellType: CellType) {
    val cells = cellType.property.get(prison)

    if (isInCell(sender, cells))
        return sender.sendMessage("<p>There's already a cell there.".mm)

    val updatedPrison = when (cellType) {
        CellType.Prisoner -> prison.copy(prisonerCells = cells + sender.location)
        CellType.Solitary -> prison.copy(solitaryCells = cells + sender.location)
    }

    updatePrison(prison.name, updatedPrison)
    sender.sendMessage("<p>Created a new cell at your location.".mm)
}

fun removeCell(sender: Player, prison: Prison, cellType: CellType, cellIndex: Int) {
    val cells = cellType.property.get(prison)

    if (cellIndex !in 1..<cells.size)
        return sender.sendMessage("<p>That cell doesn't exist.".mm)

    val cell = cells[cellIndex - 1]
    val updatedPrison = when (cellType) {
        CellType.Prisoner -> prison.copy(prisonerCells = prison.prisonerCells - cell)
        CellType.Solitary -> prison.copy(solitaryCells = prison.solitaryCells - cell)
    }

    updatePrison(prison.name, updatedPrison)
    sender.sendMessage("<p>The <s>${getOrdinal(cellIndex)}</s> $cellType cell has been removed.".mm)
}

private fun setLocation(sender: Player, prison: Prison, locationType: String) {
    val location = sender.location

    val updatedPrison = when (locationType.lowercase()) {
        "warden's office" -> prison.copy(wardenSpawn = location)
        "respawn point" -> prison.copy(respawn = location)
        "bertrude" -> prison.copy(bertrude = location)
        "black market entrance" -> prison.copy(blackMarketIn = location)
        "black market exit" -> prison.copy(blackMarketOut = location)
        else -> return sender.sendMessage("<p>Expected one of <s>${locationNames.keys.joinToString("</s>/<s>")}</s>.")
    }

    updatePrison(prison.name, updatedPrison)
    val article = if (locationNames[locationType.lowercase()] == true) "The " else ""
    val locationName = if (article.isEmpty()) locationType else locationType.lowercase()
    sender.sendMessage("<p>$article$locationName has been moved to your location.".mm)
}

private fun `setLocation$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return when (node.name) {
        "prison" -> prisonsToml.prisons.map { it.name }.filter { it.contains(input, true) }
        "locationType" -> locationNames.keys.filter { it.contains(input, true) }
        else -> emptyList()
    }
}
