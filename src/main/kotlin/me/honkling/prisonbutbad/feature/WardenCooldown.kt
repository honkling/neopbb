@file:Feature("WardenCooldown")

package me.honkling.prisonbutbad.feature

import me.honkling.commonlib.scheduler
import me.honkling.prisonbutbad.instance
import me.honkling.prisonbutbad.lib.mm
import me.honkling.prisonbutbad.wardenCooldown
import org.bukkit.Bukkit

private fun schedule(): Int {
    return scheduler.scheduleSyncRepeatingTask(instance, ::execute, 0L, 1L)
}

private fun execute() {
    if (wardenCooldown <= 0)
        return

    wardenCooldown--
}