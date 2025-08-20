package me.honkling.neopbb.schedule

import net.kyori.adventure.bossbar.BossBar.Flag

enum class Period(
    vararg val flags: Flag,
    val isSpecial: Boolean = false
) {
    RollCall,
    Breakfast,
    FreeTime,
    Lunch,
    JobTime,
    Dinner,
    CellTime,
    LightsOut(Flag.CREATE_WORLD_FOG, Flag.DARKEN_SCREEN),
    Lockdown(Flag.CREATE_WORLD_FOG, Flag.DARKEN_SCREEN, isSpecial = true);
}