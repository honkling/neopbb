@file:Listener

package me.honkling.neopbb.event

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent
import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.currentPrison
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.*
import me.honkling.neopbb.refreshTab
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.time.Duration

private fun onJoin(event: PlayerJoinEvent) {
    val player = event.player

    if (player.inSolitary)
        player.role = Role.Solitary

    player.gameMode = GameMode.ADVENTURE
    player.prepare(true, broadcast = true)
    event.joinMessage("<p><s>${player.name}</s> is now in prison.".mm)
    refreshTab()
}

private fun onQuit(event: PlayerQuitEvent) {
    val player = event.player

    if (warden == player) {
        player.role = Role.Prisoner
        player.prepare(true, broadcast = true)
        Bukkit.getServer().sendMessage("<p>The warden has left!".mm)
        wardenCooldown = 20 * 5
    }

    player.forceRespawn()
    player.cleanUp()
    event.quitMessage("<p><s>${player.name}</s> has ran off.".mm)
    refreshTab()
}

private fun onDeath(event: PlayerDeathEvent) {
    val player = event.player

    if (player.role == Role.Criminal)
        player.role = Role.Prisoner

    val attacker = event.damageSource.causingEntity as? Player
        ?: return

    if (player.isGlowing) {
        attacker.money += 100
        attacker.sendMessage("<p><s>+100$</s> for killing a glowing player.".mm)
        player.isGlowing = false
    }
}

private fun onRespawn(event: PlayerPostRespawnEvent) {
    val player = event.player
    val attacker = player.lastDamageCause?.damageSource?.causingEntity

    if (player == warden) {
        Bukkit.getServer().sendMessage("<p>The warden has died!".mm)
        player.role = Role.Prisoner
        wardenCooldown = 20 * 5
    }

    player.sendTitlePart(TitlePart.TITLE, "<red>Respawning...".mm)
    player.sendTitlePart(TitlePart.SUBTITLE, "<gray>Wait 10 seconds.".mm)
    player.sendTitlePart(TitlePart.TIMES, Title.Times.times(
        Duration.ZERO,
        Duration.ofSeconds(10),
        Duration.ZERO
    ))

    player.gameMode = GameMode.SPECTATOR
    attacker?.let { player.teleport(it) }
    val cooldown = 20L * 10
    var ticks = cooldown
    player.respawnTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, {
        ticks--

        if (ticks <= 0L) {
            player.forceRespawn()
            refreshTab()
            return@scheduleSyncRepeatingTask
        }

        if (cooldown - ticks == 8L && attacker != null)
            player.spectatorTarget = attacker

        if (attacker == null)
            player.teleport(if (player.inSolitary) currentPrison.solitary
                else currentPrison.respawn)
    }, 0L, 1L)

    refreshTab()
}

private fun onCancelSpectate(event: PlayerStopSpectatingEntityEvent) {
    if (event.player.isRespawning)
        event.isCancelled = true
}