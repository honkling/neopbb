@file:Feature("Schedule")
@file:Listener

package me.honkling.neopbb.feature

import com.github.retrooper.packetevents.protocol.potion.PotionTypes
import com.github.retrooper.packetevents.protocol.potion.Potions
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect
import me.honkling.commando.spigot.event.Listener
import me.honkling.commonlib.scheduler
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.clamp
import me.honkling.neopbb.lib.good
import me.honkling.neopbb.lib.onlinePlayers
import me.honkling.neopbb.packetEvents
import me.honkling.neopbb.prison
import me.honkling.neopbb.profile.profile
import me.honkling.neopbb.schedule.Period
import me.honkling.neopbb.schedule.scheduleHours
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionType
import java.time.Duration

private var period = Period.RollCall
    set(value) {
        if (field == Period.RollCall && field != value)
            for (player in onlinePlayers) {
                val profile = player.profile
                println("${player.name} (${profile.role.isAuthority}/${profile.hasAttendedRollCall})")

                if (profile.role.isAuthority || profile.hasAttendedRollCall)
                    continue

                println("Marking as in trouble")
                profile.isInTrouble = true
                profile.hasAttendedRollCall = false
            }

        field = value

        for ((player, bossBar) in bossBars) {
            player.profile.hasAttendedRollCall = false
            bossBar.flags(value.flags.toMutableSet())
        }
    }

private val bossBars = mutableMapOf<Player, BossBar>()

private val worldTimer
    get() = (prison.world.fullTime / 20 + (6 * 60)).mod(24 * 60).toLong()

private fun schedule(): Int {
    for (player in onlinePlayers)
        player.showBossBar(createBossBar(player))

    return scheduler.scheduleSyncRepeatingTask(instance, ::execute, 0L, 20L)
}

private fun execute() {
    val timer = worldTimer
    val hour = timer / 60
    val minutes = timer.mod(60)

    val newPeriod = scheduleHours.entries.find { it.key.first == hour.toInt() }
    if (newPeriod != null && minutes == 0)
        period = newPeriod.value

    // Time changed from commands, we need to correct the hour
    if (hour !in scheduleHours.entries.find { it.value == period }!!.key)
        period = scheduleHours.entries.find { hour in it.key }!!.value

//    println("$previousPeriod -> $period")

    val isRollCall = period == Period.RollCall
    for (player in onlinePlayers) {
        val bossBar = bossBars[player]
            ?: continue

        if (isRollCall) {
            val profile = player.profile

            if (profile.role.isAuthority)
                continue

            if (player.location.subtract(0.0, 1.0, 0.0).block.type == Material.RED_SAND)
                profile.hasAttendedRollCall = true

            if (!profile.hasAttendedRollCall) {
                player.sendTitlePart(TitlePart.TITLE, Component.empty())
                player.sendTitlePart(TitlePart.SUBTITLE, Component.text("Go to the red sand for roll call!")
                    .color(NamedTextColor.RED))
                player.sendTitlePart(TitlePart.TIMES, Title.Times.times(
                    Duration.ZERO,
                    Duration.ofMillis(1050),
                    Duration.ofMillis(0)
                ))
            }
        }

        bossBar.name(bossBarName(player, timer))
            .progress(bossBarProgress(timer))
    }
}

private fun onPlayerJoin(event: PlayerJoinEvent) {
    val player = event.player
    player.showBossBar(createBossBar(player))
}

private fun onPlayerQuit(event: PlayerQuitEvent) {
    val player = event.player
    val bossBar = bossBars[player]
        ?: return

    player.hideBossBar(bossBar)
    bossBars -= player
}

private fun createBossBar(player: Player): BossBar {
    val bossBar = BossBar.bossBar(
        bossBarName(player, worldTimer),
        bossBarProgress(worldTimer),
        BossBar.Color.WHITE,
        BossBar.Overlay.PROGRESS
    )

    bossBars[player] = bossBar
    return bossBar
}

private fun bossBarName(player: Player, timer: Long): Component {
    val hours = timer.div(60).toString().padStart(2, '0')
    val minutes = timer.mod(60).toString().padStart(2, '0')
    val rollCall = period == Period.RollCall && player.profile.hasAttendedRollCall

    return Component.empty()
        .append(period.title
            .decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.TRUE))
        .append(Component.space())
        .append(if (!rollCall) Component.empty()
            else Component.text("✔ ")
                .decorate(TextDecoration.BOLD)
                .color(good))
        .append(Component.text("($hours:$minutes)")
            .color(NamedTextColor.GRAY))
}

private fun bossBarProgress(timer: Long): Float {
    val hour = timer / 60
    val period = scheduleHours.entries.find { hour in it.key }!!
    val periodHour = period.key.first
    var nextPeriod = scheduleHours.keys.filter { it.first > hour }.minByOrNull { it.first }?.first
        ?: scheduleHours.keys.first().first
    val elapsed = if (periodHour * 60 > timer) timer + 24 * 60 - periodHour * 60
                  else timer - periodHour * 60

    if (nextPeriod < periodHour)
        nextPeriod -= 24

    return (elapsed / (60f * (nextPeriod - periodHour))).clamp(0f, 1f)
}