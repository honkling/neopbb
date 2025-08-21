package me.honkling.neopbb

import io.netty.util.internal.ThreadLocalRandom
import me.honkling.neopbb.config.PrisonsToml
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.role
import org.bukkit.Bukkit
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

lateinit var currentPrison: PrisonsToml.Prison; internal set
var lastLockdown = 0L
var lastMapSwitch = 0L

@OptIn(ExperimentalTime::class)
fun switchMap(newPrison: PrisonsToml.Prison) {
    currentPrison = newPrison
    lastMapSwitch = Clock.System.now().epochSeconds

    for (player in Bukkit.getOnlinePlayers()) {
        val role = player.role
        player.teleport(when (role) {
            Role.Warden -> newPrison.wardenSpawn
            else -> {
                val random = ThreadLocalRandom.current();
                val randomCellIndex = random.nextInt(0, newPrison.cells.size)

                newPrison.cells[randomCellIndex]
            }
        })
    }
}