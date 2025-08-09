@file:Command("tutorial")

package me.honkling.neopbb.command

import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.tutorial.highlightBlocks
import me.honkling.neopbb.tutorial.setPointers
import org.bukkit.Location
import org.bukkit.entity.Player

private fun tutorial(player: Player) {
    player.setPointers(Location(player.world, 0.0, 0.0, 0.0))
    player.highlightBlocks(player.world.getBlockAt(player.location.subtract(0.0, 1.0, 0.0)))
    player.sendMessage("ok")
}