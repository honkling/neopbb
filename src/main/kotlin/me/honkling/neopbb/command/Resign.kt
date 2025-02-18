@file:Command("resign")

package me.honkling.neopbb.command

import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.profile.profile
import org.bukkit.entity.Player

fun resign(sender: Player) {
    sender.profile.reset()
}