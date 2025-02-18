@file:Feature("WardenCooldown")

package me.honkling.neopbb.feature

import me.honkling.commonlib.scheduler
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.onlinePlayers
import me.honkling.neopbb.profile.profile
import me.honkling.neopbb.wardenCooldown

private fun schedule(): Int {
    return scheduler.scheduleSyncRepeatingTask(instance, ::execute, 0L, 1L)
}

private fun execute() {
    if (wardenCooldown > 0)
        wardenCooldown--

    for (player in onlinePlayers) {
        val profile = player.profile

        if (profile.timeSinceLastWarden >= 0)
            profile.timeSinceLastWarden++
    }
}