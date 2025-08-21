package me.honkling.neopbb.task

import me.honkling.neopbb.currentPrison
import me.honkling.neopbb.lib.isInCell
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

        if(role.isAuthority || role == Role.Criminal || role == Role.Solitary) continue
        if(player.hasPotionEffect(PotionEffectType.GLOWING) && player.getPotionEffect(PotionEffectType.GLOWING)?.duration!! >= 5) continue

        val isInCell = isInCell(player, currentPrison.prisonerCells)

        player.inCell = isInCell

        if(isInCell) {
            player.sendTitlePart(TitlePart.TITLE, Component.empty())
            player.sendTitlePart(TitlePart.SUBTITLE, Component.empty())
            continue
        }

        player.addPotionEffect(PotionEffectType.GLOWING.createEffect(5, 0))
    }
}