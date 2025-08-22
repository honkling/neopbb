@file:Command("debug", permission = "neopbb.debug")

package me.honkling.neopbb.command

import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.config.prisonsToml
import me.honkling.neopbb.lib.formatCurrency
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.money
import me.honkling.neopbb.profile.prepare
import me.honkling.neopbb.profile.role
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private fun setMoney(sender: CommandSender, player: Player, money: Float) {
    player.money = money
    sender.sendMessage("<p><s>${player.name}</s> now has <s>${formatCurrency(money)}</s>.".mm)
}

private fun setRole(sender: CommandSender, player: Player, role: Role, withKit: Boolean) {
    player.role = role
    sender.sendMessage("<p><s>${player.name}</s>'s role is now set to <s>${role.name}</s>.".mm)

    if(withKit)
        player.prepare(true, broadcast = false)
}

// === Debug Builder Commands ===

private fun teleportCell(sender: CommandSender, player: Player, prisonName: String, cellType: String, cellIndex: Int) {
    val prison = prisonsToml.prisons.find { prisonName.equals(it.name, ignoreCase = true) }
        ?: return sender.sendMessage("<p>The prison named <s>$prisonName</s> is not found.".mm)

    val cells = when (cellType.lowercase()) {
        "prisoner" -> prison.prisonerCells
        "solitary" -> prison.solitaryCells
        else -> return sender.sendMessage("<p>You must provide either <s>prisoner</s> or <s>solitary</s> as the cell type.".mm)
    }

    val cellCount = "<s>[${cellIndex}/${cells.size-1}]</s>"

    if(cells.isEmpty())
        return sender.sendMessage("<p>The prison named <s>$prisonName</s> does not have any $cellType cells. $cellCount".mm)

    if(cellIndex >= cells.size)
        return  sender.sendMessage("<p>The prison named <s>$prisonName</s> does not have that many $cellType cells. $cellCount".mm)

    val cellLocation = cells[cellIndex]

    player.teleport(cellLocation)
    player.sendMessage("<p>You have been teleported to the prison named <s>$prisonName</s>'s $cellType cell number <s>$cellIndex</s>. $cellCount".mm)
}

private fun `teleportCell$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return when (node.name) {
        "prisonName" -> prisonsToml.prisons.map { it.name }
        "player" -> Bukkit.getOnlinePlayers().map { it.name }
        "cellType" -> listOf("solitary", "prisoner")
        else -> emptyList()
    }
}

private fun teleportLocation(sender: Player,  player: Player, prisonName: String, locationType: String) {
    val prison = prisonsToml.prisons.find { it.name.equals(prisonName, ignoreCase = true) }
        ?: return sender.sendMessage("<p>There is no prison by the name <s>$prisonName</s>.".mm)

    val targetLocation = when (locationType.lowercase()) {
        "warden" -> prison.wardenSpawn
        "respawn" -> prison.respawn
        "bertrude" -> prison.bertrude
        "blackmarketin" -> prison.blackMarketIn
        "blackmarketout" -> prison.blackMarketOut
        else -> return sender.sendMessage(("<p>You must provide one of the following " +
                "<s>warden</s>, <s>respawn</s>, <s>bertrude</s>, <s>blackmarketin</s>, and <s>blackmarketout</s>" +
                " as the locationType.").mm)
    }

    player.teleport(targetLocation)
    player.sendMessage("<p>You have been teleported to the prison named <s>$prisonName</s>'s <s>$locationType</s>.".mm)
    if(player != sender)
        sender.sendMessage("<p>You have teleported ${player.name} to the prison named <s>$prisonName</s>'s <s>$locationType</s> location.".mm)
}

private fun `teleportLocation$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return when (node.name) {
        "prisonName" -> prisonsToml.prisons.map { it.name }
        "player" -> Bukkit.getOnlinePlayers().map { it.name }
        "locationType" -> listOf("warden", "respawn", "bertrude", "blackmarketin", "blackmarketout")
        else -> emptyList()
    }
}