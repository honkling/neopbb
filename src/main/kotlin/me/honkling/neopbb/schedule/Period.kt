package me.honkling.neopbb.schedule

import me.honkling.neopbb.lib.mm
import net.kyori.adventure.bossbar.BossBar.Flag

enum class Period(
    title: String,
    vararg val flags: Flag,
    val isSpecial: Boolean = false
) {
    RollCall("Roll Call"),
    Breakfast("Breakfast"),
    FreeTime("Free Time"),
    JobTime("Job Time"),
    Lunch("Lunch"),
    CellTime("Cell Time"),
    LightsOut("Lights Out", Flag.CREATE_WORLD_FOG, Flag.DARKEN_SCREEN),
    Lockdown("<red>Lockdown</red>", Flag.CREATE_WORLD_FOG, Flag.DARKEN_SCREEN, isSpecial = true);

    val title = title.mm
}