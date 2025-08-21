package me.honkling.neopbb.task

import me.honkling.neopbb.currentPrison
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.inCell
import me.honkling.neopbb.profile.role
import me.honkling.neopbb.schedule.Period
import me.honkling.neopbb.schedule.period
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.potion.PotionEffectType

internal fun executeLightsOut() {
    if(!(period == Period.LightsOut || period == Period.Lockdown))
        return

    loop@ for (player in Bukkit.getOnlinePlayers()) {
        val role = player.role

        if(role.isAuthority || role == Role.Criminal) continue
        if(player.hasPotionEffect(PotionEffectType.GLOWING) && player.getPotionEffect(PotionEffectType.GLOWING)?.duration!! >= 5) continue

        val playerLocation = player.location

        val isInCell = currentPrison.cells.any { loc ->
            if (loc.world != playerLocation.world) continue@loop

            val dx = playerLocation.blockX - loc.blockX
            val dy = playerLocation.blockY - loc.blockY
            val dz = playerLocation.blockZ - loc.blockZ

            dx in -1..1 && dy in -1..1 && dz in -1..1
        }

        player.inCell = isInCell

        if(isInCell) {
            player.sendTitlePart(TitlePart.TITLE, Component.empty())
            player.sendTitlePart(TitlePart.SUBTITLE, Component.empty())
            continue
        }

        player.addPotionEffect(PotionEffectType.GLOWING.createEffect(5, 0))
    }
}