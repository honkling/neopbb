@file:Command("teamchat", "tc")

package me.honkling.neopbb.command

import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.lib.onlinePlayers
import me.honkling.neopbb.profile.profile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.entity.Player

private fun teamchat(sender: Player) {
    val isInTeamChat = !sender.profile.isInTeamChat
    sender.profile.isInTeamChat = isInTeamChat

    sender.sendMessage((if (isInTeamChat)
        "<gray>You are now in team chat."
    else "<gray>You are no longer in team chat.").mm)
}

fun teamchat(sender: Player, message: String) {
    val isAuthority = sender.profile.role.isAuthority
    val teamMembers = onlinePlayers.filter { it.profile.role.isAuthority == isAuthority }
    val team =
        if (!isAuthority) Component.text("Prisoner")
            .color(NamedTextColor.GOLD)
        else Component.text("Guard")
            .color(NamedTextColor.BLUE)

    val component = "<gray>[<team>] ${sender.name}: $message".mm(
        Placeholder.component("team", team.append(Component.text(" Chat")))
    )

    for (member in teamMembers)
        member.sendMessage(component)

    Bukkit.getConsoleSender().sendMessage(component)
}