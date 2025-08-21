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

private fun teleportCell(sender: CommandSender, player: Player, prisonName: String, cellIndex: Int) {
    val prison = prisonsToml.prisons.find { prisonName.equals(it.name, ignoreCase = true) }
        ?: return sender.sendMessage("<p>The prison named <s>$prisonName</s> is not found.".mm)

    val prisonCellCount = "<s>[${cellIndex}/${prison.cells.size-1}]</s>"

    if(prison.cells.isEmpty())
        return sender.sendMessage("<p>The prison named <s>$prisonName</s> does not have any cells. $prisonCellCount".mm)

    if(cellIndex >= prison.cells.size)
        return  sender.sendMessage("<p>The prison named <s>$prisonName</s> does not have that many cells. $prisonCellCount".mm)

    val cellLocation = prison.cells[cellIndex]

    player.teleport(cellLocation)
    player.sendMessage("<p>You have been teleported to the prison named <s>$prisonName</s> cell number <s>$cellIndex</s>. $prisonCellCount".mm)
}

private fun `teleportCell$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return when (node.name) {
        "prisonName" -> prisonsToml.prisons.map { it.name }
        "player" -> Bukkit.getOnlinePlayers().map { it.name }
        else -> emptyList()
    }
}