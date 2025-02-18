@file:Listener

package me.honkling.neopbb.event

import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.lib.onlinePlayers
import me.honkling.neopbb.lib.troubleTeam
import me.honkling.neopbb.profile.profile
import me.honkling.neopbb.profile.removeProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

private fun onPlayerJoin(event: PlayerJoinEvent) {
    val player = event.player
    player.inventory.clear()
    troubleTeam.addPlayer(player)
    event.joinMessage(Component.text("${player.name} has been sent to prison.")
        .color(NamedTextColor.GOLD))

    for (aPlayer in onlinePlayers) {
        val profile = aPlayer.profile

        if (profile.isInTrouble)
            profile.syncInTrouble(player)
    }
}

private fun onPlayerQuit(event: PlayerQuitEvent) {
    val player = event.player
    player.inventory.clear()
    player.removeProfile()
    event.quitMessage(Component.text("${player.name} has ran off...")
        .color(NamedTextColor.GOLD))
}