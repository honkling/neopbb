package me.honkling.neopbb.lib

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit

private val scoreboardManager = Bukkit.getScoreboardManager()
private val scoreboard = scoreboardManager.mainScoreboard
val troubleTeam = (scoreboard.getTeam("trouble")
    ?: scoreboard.registerNewTeam("trouble")).also {
        it.color(NamedTextColor.DARK_RED)
    }