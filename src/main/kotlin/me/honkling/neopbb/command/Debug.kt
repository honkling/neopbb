@file:Command("debug", permission = "neopbb.debug")

package me.honkling.neopbb.command

import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.lib.formatCurrency
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.money
import me.honkling.neopbb.profile.prepare
import me.honkling.neopbb.profile.role
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private fun money(sender: CommandSender, player: Player, money: Float) {
    player.money = money
    sender.sendMessage("<p><s>${player.name}</s> now has <s>${formatCurrency(money)}</s>.".mm)
}

private fun role(sender: CommandSender, player: Player, role: Role, withKit: Boolean = true) {
    player.role = role
    val article = if (role == Role.Warden) "the" else "a"
    sender.sendMessage("<p><s>${player.name}</s> is now $article ${role.name.lowercase()}.".mm)

    if (withKit)
        player.prepare(true)
}