package me.honkling.neopbb.schedule

import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.*
import me.honkling.neopbb.world
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.Math.clamp
import java.time.Duration

private val pascalCaseRegex = Regex("[A-Z][a-z]+")

var period = Period.RollCall
    set(value) {
        field = value

        for ((_, bossBar) in bossBars)
            bossBar.flags(value.flags.toMutableSet())
    }

internal val bossBars = mutableMapOf<Player, BossBar>()

val scheduleHours = mapOf(
    0..<4 to Period.LightsOut,
    4..<6 to Period.RollCall,
    6..<8 to Period.Breakfast,
    8..<11 to Period.FreeTime,
    11..<13 to Period.Lunch,
    13..<16 to Period.JobTime,
    16..<18 to Period.Lunch,
    18..<20 to Period.RollCall,
    20..<21 to Period.CellTime,
    21..24 to Period.LightsOut
)

private val worldTimer
    get() = (world.time / 20 + (6 * 60)).mod(24 * 60).toLong()

fun registerScheduler() {
    for (player in Bukkit.getOnlinePlayers())
        player.showBossBar(createBossBar(player))

    Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, ::tickSchedule, 0L, 20L)
}

internal fun tickSchedule() {
    val timer = worldTimer
    val hour = timer / 60
    val minutes = timer.mod(60)

    var periodChanged = false
    val oldPeriod = period
    val newPeriod = scheduleHours.entries.find { it.key.first == hour.toInt() }
    if (newPeriod != null && minutes == 0) {
        period = newPeriod.value
        periodChanged = true
    }

    // Time changed from commands, we need to correct the hour
    if (period != Period.Lockdown && scheduleHours.entries.none { it.value == period && hour in it.key }) {
        period = scheduleHours.entries.find { hour in it.key }!!.value
        periodChanged = true
    }

    if (oldPeriod == Period.RollCall && period != Period.RollCall && periodChanged) {
        val guiltyPeople = Bukkit.getOnlinePlayers().filter { !it.attendedRollCall && it.role == Role.Prisoner }

        if (guiltyPeople.isEmpty()) {
            warden?.money += 1000
            warden?.sendMessage("<p><s>+1000$</s> for all players attending roll call.".mm)
        } else {
            guiltyPeople.forEach { it.isGlowing = true }
            Bukkit.getServer().sendMessage(
                "<p>These people didn't attend roll call, kill them for a reward:\n<p><s>${guiltyPeople.joinToString("</s>, <s>", transform = Player::getName)}".mm
            )
        }
    }

    for (player in Bukkit.getOnlinePlayers()) {
        val bossBar = bossBars[player]
            ?: continue

        if (periodChanged) {
            player.playSound(Sound.sound {
                it.type(Key.key("block.bell.use"))
            })
            player.attendedRollCall = false
            player.sendTitlePart(TitlePart.TITLE, Component.empty())
            player.sendTitlePart(TitlePart.SUBTITLE, Component.empty())
        }

        if (period == Period.RollCall && !player.attendedRollCall && !player.role.isAuthority) {
            player.sendTitlePart(TitlePart.TIMES, Title.Times.times(
                Duration.ZERO,
                Duration.ofSeconds(2L),
                Duration.ZERO
            ))
            player.sendTitlePart(TitlePart.TITLE, Component.empty())
            player.sendTitlePart(TitlePart.SUBTITLE, "<red>Go to the red sand or you'll be killed!".mm)
        }

        if ((period == Period.Lockdown || period == Period.LightsOut) && !player.role.isAuthority && !player.inCell) {
            player.sendTitlePart(TitlePart.TIMES, Title.Times.times(
                Duration.ZERO,
                Duration.ofSeconds(2L),
                Duration.ZERO
            ))
            player.sendTitlePart(TitlePart.TITLE, Component.empty())
            player.sendTitlePart(TitlePart.SUBTITLE, "<red>Go to your cell or you'll be killed!".mm)
        }

        if (period == Period.Breakfast || period == Period.Lunch || period == Period.Dinner)
            for (player in Bukkit.getOnlinePlayers()) {
                player.foodLevel = 20
            }

        bossBar.name(bossBarName(player, timer))
            .progress(bossBarProgress(timer))
    }
}

internal fun createBossBar(player: Player): BossBar {
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

    val periodName = Component.text(pascalCaseRegex.findAll(period.name)
        .joinToString(" ") { it.value })

    val rollCallCheck = if (period == Period.RollCall && player.attendedRollCall)
        Component.text("✔ ")
            .color(NamedTextColor.GREEN)
    else Component.empty()

    return Component.empty()
        .append(rollCallCheck)
        .append(periodName
            .decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.TRUE))
        .append(Component.space())
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

    return clamp((elapsed / (60f * (nextPeriod - periodHour))), 0f, 1f)
}