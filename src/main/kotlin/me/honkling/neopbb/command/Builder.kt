@file:Command("builder", permission = "neopbb.builder")

package me.honkling.neopbb.command

import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.config.prisonsToml
import me.honkling.neopbb.config.savePrisonsToml
import me.honkling.neopbb.config.updatePrison
import me.honkling.neopbb.lib.isInCell
import me.honkling.neopbb.lib.mm
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private fun setIcon(sender: Player, prisonName: String, icon: Material){
    val prison = prisonsToml.prisons.find { it.name.equals(prisonName, ignoreCase = true) }
        ?: return sender.sendMessage("<p>There prison named <s>$prisonName</s> does not exist.".mm)

    val newPrison = prison.copy(icon = icon)

    updatePrison(prisonName, newPrison)

    savePrisonsToml()
    sender.sendMessage("<p>The prison named <s>$prisonName</s>'s icon has been set to <s>${icon.name}</s>.".mm)
}

private fun `setIcon$complete`(sender: Player, node: ParameterNode<Command>, input: String): List<String> {
    return when (node.name) {
        "prisonName" -> prisonsToml.prisons.map { it.name } .filter { it.contains(input, true) }
        "icon" -> Material.entries.map { it.toString() } .filter { it.contains(input, true) }
        else -> emptyList()
    }
}


fun addCell(sender: Player, prisonName: String, cellType: String) {
    val prison = prisonsToml.prisons.find { it.name.equals(prisonName, ignoreCase = true) }
        ?: return sender.sendMessage("<p>There prison named <s>$prisonName</s> does not exist.".mm)

    val cells = when (cellType.lowercase()) {
        "prisoner" -> prison.prisonerCells
        "solitary" -> prison.solitaryCells
        else -> return sender.sendMessage("<p>You must provide either <s>prisoner</s> or <s>solitary</s> as the cell type.".mm)
    }

    if(isInCell(sender, cells))
        return sender.sendMessage("<p>The prison named <s>$prisonName</s> already has a <s>$cellType</s> cell at your location.")

    val updatedPrison = when (cellType.lowercase()) {
        "prisoner" -> prison.copy(prisonerCells = cells + sender.location)
        "solitary" -> prison.copy(solitaryCells = cells + sender.location)
        else -> return sender.sendMessage("<p>You must provide either <s>prisoner</s> or <s>solitary</s> as the cell type.".mm)
    }

    updatePrison(prisonName, updatedPrison)
    sender.sendMessage("<p>The prison named <s>$prisonName</s>'s new <s>$cellType</s> cell at your location.".mm)
}

private fun `addCell$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return when(node.name){
        "prisonName" -> prisonsToml.prisons.map { it.name } .filter { it.contains(input, true) }
        "cellType" -> listOf("solitary", "prisoner") .filter { it.contains(input, true) }
        else -> emptyList()
    }
}

fun removeCell(sender: Player, prisonName: String, cellType: String, cellIndex: Int) {
    val prison = prisonsToml.prisons.find { it.name.equals(prisonName, ignoreCase = true) }
        ?: return sender.sendMessage("<p>There prison named <s>$prisonName</s> does not exist.".mm)

    val updatedPrison = when (cellType.lowercase()) {
        "prisoner" -> {
            if (cellIndex !in prison.prisonerCells.indices)
                return sender.sendMessage("<p>The prison named <s>$prisonName</s>'s <s>$cellType</s> cells, has only <s>0-${prison.prisonerCells.size - 1}</s> $cellType cells.".mm)
            prison.copy(prisonerCells = prison.prisonerCells.toMutableList().apply { removeAt(cellIndex) })
        }
        "solitary" -> {
            if (cellIndex !in prison.solitaryCells.indices)
                return sender.sendMessage("<p>The prison named <s>$prisonName</s>'s <s>$cellType</s> cells, has only <s>0-${prison.solitaryCells.size - 1}</s> $cellType cells.".mm)
            prison.copy(solitaryCells = prison.solitaryCells.toMutableList().apply { removeAt(cellIndex) })
        }
        else -> return sender.sendMessage("<p>You must provide either <s>prisoner</s> or <s>solitary</s> as the cell type.".mm)
    }

    updatePrison(prisonName, updatedPrison)
    sender.sendMessage("<p>The prison named <s>$prisonName</s>'s <s>$cellType</s> cell #<s>$cellIndex</s> has been removed.".mm)
}

private fun `removeCell$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return when(node.name){
        "prisonName" -> prisonsToml.prisons.map { it.name } .filter { it.contains(input, true) }
        "cellType" -> listOf("solitary", "prisoner") .filter { it.contains(input, true) }
        else -> emptyList()
    }
}

private fun setLocation(sender: Player, prisonName: String, locationType: String) {
    val prison = prisonsToml.prisons.find { it.name.equals(prisonName, ignoreCase = true) }
        ?: return sender.sendMessage("<p>There prison named <s>$prisonName</s> does not exist.".mm)

    val loc = sender.location

    val updatedPrison = when (locationType.lowercase()) {
        "warden" -> prison.copy(wardenSpawn = loc)
        "respawn" -> prison.copy(respawn = loc)
        "bertrude" -> prison.copy(bertrude = loc)
        "blackmarketin" -> prison.copy(blackMarketIn = loc)
        "blackmarketout" -> prison.copy(blackMarketOut = loc)
        else -> return sender.sendMessage(("<p>You must provide one of the following " +
                "<s>warden</s>, <s>respawn</s>, <s>bertrude</s>, <s>blackmarketin</s>, and <s>blackmarketout</s>" +
                " as the locationType.").mm)
    }

    updatePrison(prisonName, updatedPrison)
    sender.sendMessage("<p>The prison named <s>$prisonName</s>'s <s>$locationType</s> is now at your location.".mm)
}

private fun `setLocation$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return when (node.name) {
        "prisonName" -> prisonsToml.prisons.map { it.name } .filter { it.contains(input, true) }
        "locationType" -> listOf("warden", "respawn", "bertrude", "blackMarketIn", "blackMarketOut") .filter { it.contains(input, true) }
        else -> emptyList()
    }
}
