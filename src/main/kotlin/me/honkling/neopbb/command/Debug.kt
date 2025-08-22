@file:Command("debug", permission = "neopbb.debug")

package me.honkling.neopbb.command

import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.config.PrisonsToml.Prison
import me.honkling.neopbb.config.prisonsToml
import me.honkling.neopbb.lib.CellType
import me.honkling.neopbb.lib.formatCurrency
import me.honkling.neopbb.lib.getOrdinal
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.money
import me.honkling.neopbb.profile.prepare
import me.honkling.neopbb.profile.role
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

internal val locationNames = mapOf(
    "warden's office" to true,
    "respawn point" to true,
    "bertrude" to false,
    "black market entrance" to true,
    "black market exit" to true
)

private fun setMoney(sender: CommandSender, player: Player, money: Float) {
    player.money = money
    sender.sendMessage("<p><s>${player.name}</s> now has <s>${formatCurrency(money)}</s>.".mm)
}

private fun setRole(sender: CommandSender, player: Player, role: Role, withKit: Boolean) {
    player.role = role
    sender.sendMessage("<p><s>${player.name}</s>'s role is now set to <s>${role.name}</s>.".mm)

    if (withKit)
        player.prepare(true, broadcast = false)
}

// === Debug Builder Commands ===

private fun teleportCell(sender: CommandSender, player: Player, prison: Prison, cellType: CellType, cellIndex: Int) {
    val cells = cellType.property.get(prison)
    val cellTypeName = cellType.name.lowercase()

    if (cells.isEmpty())
        return sender.sendMessage("<p>That prison does not have any $cellTypeName cells.".mm)

    if (cellIndex !in 1..cells.size)
        return sender.sendMessage("<p>That $cellTypeName cell does not exist.".mm)

    player.teleport(cells[cellIndex - 1])
    player.sendMessage("<p>You've been teleported to the <s>${getOrdinal(cellIndex)}</s> $cellTypeName cell.".mm)
}

private fun teleportLocation(sender: Player, player: Player, prison: Prison, locationType: String) {
    val targetLocation = when (locationType.lowercase()) {
        "warden's office" -> prison.wardenSpawn
        "respawn point" -> prison.respawn
        "bertrude" -> prison.bertrude
        "black market entrance" -> prison.blackMarketIn
        "black market exit" -> prison.blackMarketOut
        else -> return sender.sendMessage("<p>Expected one of <s>${locationNames.keys.joinToString("</s>/<s>")}</s>.")
    }

    player.teleport(targetLocation)
    val article = if (locationNames[locationType.lowercase()] == true) "the " else ""
    player.sendMessage("<p>You have been teleported to $article$locationType.".mm)

    if (player != sender)
        sender.sendMessage("<p><s>${player.name}</s> has been teleported to $locationType.".mm)
}

private fun `teleportLocation$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return when (node.name) {
        "locationType" -> locationNames.keys.toList()
        else -> emptyList()
    }
}