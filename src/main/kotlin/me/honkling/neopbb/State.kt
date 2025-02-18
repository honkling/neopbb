package me.honkling.neopbb

import me.honkling.neopbb.config.fallbackPrison
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.profile
import org.bukkit.Bukkit
import org.bukkit.entity.Player

var prison = fallbackPrison

val wardens: List<Player>
    get() = Bukkit.getOnlinePlayers().filter { it.profile.role == Role.Warden }
var wardenCooldown = 0