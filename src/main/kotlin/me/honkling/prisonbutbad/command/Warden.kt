@file:Command("warden")

package me.honkling.prisonbutbad.command

import me.honkling.commando.spigot.command.Command
import me.honkling.prisonbutbad.lib.mm
import me.honkling.prisonbutbad.profile.Role
import me.honkling.prisonbutbad.profile.profile
import me.honkling.prisonbutbad.wardens
import org.bukkit.Bukkit
import org.bukkit.entity.Player

private fun warden(sender: Player) {
    if (wardens.isNotEmpty())
        return sender.sendMessage("<failure>There is already a warden.".mm)

    for (player in Bukkit.getOnlinePlayers())
        if (player.profile.role.isAuthority)
            player.profile.reset()

    sender.profile.role = Role.Warden
}