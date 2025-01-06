@file:Command("resign")

package me.honkling.prisonbutbad.command

import me.honkling.commando.spigot.command.Command
import me.honkling.prisonbutbad.profile.profile
import org.bukkit.entity.Player

fun resign(sender: Player) {
    sender.profile.reset()
}