@file:Command("resign")

package me.honkling.neopbb.command

import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player

private fun resign(player: Player) {
    if (player.isRespawning)
        return player.sendMessage("<p>You cannot resign while respawning.".mm)

    if (player.inSolitary)
        return player.sendMessage("<p>You cannot resign while in solitary.".mm)

    if (warden == player) {
        Bukkit.getServer().sendMessage("<p>The warden has resigned!".mm)
        wardenCooldown = 20 * 5
    }

    player.role = Role.Prisoner
    player.prepare(true, broadcast = true)
}