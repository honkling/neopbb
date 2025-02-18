@file:Command("accept")

package me.honkling.neopbb.command

import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.profile
import org.bukkit.entity.Player

private fun accept(sender: Player) {
    val profile = sender.profile
    val (warden, role) = profile.invite
        ?: return sender.sendMessage("<failure>You don't have any invitations.".mm)

    if (role == Role.Warden) {
        warden.profile.reset()
        profile.role = Role.Warden
        return
    }

    profile.role = role
}