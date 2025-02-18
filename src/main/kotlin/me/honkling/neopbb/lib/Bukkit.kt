package me.honkling.neopbb.lib

import org.bukkit.Bukkit

const val namespace = "neopbb"

val onlinePlayers
    get() = Bukkit.getOnlinePlayers()