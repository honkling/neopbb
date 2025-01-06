package me.honkling.prisonbutbad

import me.honkling.prisonbutbad.profile.Role
import me.honkling.prisonbutbad.profile.profile
import org.bukkit.Bukkit
import org.bukkit.entity.Player

val wardens: List<Player>
    get() = Bukkit.getOnlinePlayers().filter { it.profile.role == Role.Warden }
var wardenCooldown = 0