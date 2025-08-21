package me.honkling.neopbb.task

import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.attendedRollCall
import me.honkling.neopbb.profile.role
import me.honkling.neopbb.schedule.Period
import me.honkling.neopbb.schedule.period
import me.honkling.neopbb.schedule.tickSchedule
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.potion.PotionEffectType

internal fun executeRollCall() {
    if (period != Period.RollCall)
        return

    for (player in Bukkit.getOnlinePlayers()) {
        if (player.attendedRollCall || player.role.isAuthority || player.role == Role.Criminal || player.role == Role.Solitary)
            continue

        val below = player.location.clone().add(0.0, -1.0, 0.0)
        val block = player.world.getBlockAt(below).type

        if (block == Material.RED_SAND || block == Material.RED_SANDSTONE || block == Material.CUT_RED_SANDSTONE) {
            player.attendedRollCall = true
            player.sendTitlePart(TitlePart.SUBTITLE, Component.empty())
            player.playSound(Sound.sound {
                it.type(Key.key("block.note_block.chime"))
            })
            tickSchedule()
        }

        player.addPotionEffect(PotionEffectType.GLOWING.createEffect(5, 0))
    }
}