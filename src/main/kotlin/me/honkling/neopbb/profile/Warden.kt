package me.honkling.neopbb.profile

import org.bukkit.Bukkit
import org.bukkit.entity.Player

var lastWarden: Player? = null
val warden: Player?
    get() = Bukkit.getOnlinePlayers().find { it.role == Role.Warden }

var lastSolitary = 0L
var wardenStart = 0L
var wardenCooldown = 0
var swatUnlocked = false
