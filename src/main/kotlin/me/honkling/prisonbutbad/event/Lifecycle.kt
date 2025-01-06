@file:Listener

package me.honkling.prisonbutbad.event

import me.honkling.commando.spigot.event.Listener
import me.honkling.prisonbutbad.profile.removeProfile
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

private fun onPlayerJoin(event: PlayerJoinEvent) {

}

private fun onPlayerQuit(event: PlayerQuitEvent) {
    val player = event.player
    player.removeProfile()
}