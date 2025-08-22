package me.honkling.neopbb.task

import me.honkling.neopbb.currentPrison
import me.honkling.neopbb.lib.isInCell
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.role
import me.honkling.neopbb.profile.solitaryTask
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.potion.PotionEffectType

internal fun executeSolitary() {
    val solitaryPlayers = Bukkit.getOnlinePlayers().filter { it.role == Role.Solitary }

    for (player in solitaryPlayers) {
        if (isInCell(player, currentPrison.solitaryCells))
            continue

        player.role = Role.Prisoner
        player.role.team.addPlayer(player) // Doesn't swap team without prepare...
        player.solitaryTask?.let { Bukkit.getScheduler().cancelTask(it) }

        val guards = Audience.audience(Bukkit.getOnlinePlayers().filter { it.role.isAuthority })
        player.addPotionEffect(PotionEffectType.GLOWING.createEffect(20 * 30, 0))
        player.sendMessage("<p>You were caught escaping solitary!".mm)
        guards.sendMessage("<p><s>${player.name}</s> was caught escaping solitary!".mm)
    }
}