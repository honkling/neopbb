package me.honkling.neopbb.task

import me.honkling.neopbb.instance
import me.honkling.neopbb.refreshTab
import org.bukkit.Bukkit

fun registerTasks() {
    val scheduler = Bukkit.getScheduler()
    scheduler.scheduleSyncRepeatingTask(instance, ::refreshTab, 0L, 20L)
    scheduler.scheduleSyncRepeatingTask(instance, {
        executeRollCall()
        executeActionBar()
        executeLightsOut()
    }, 0L, 1L)
}