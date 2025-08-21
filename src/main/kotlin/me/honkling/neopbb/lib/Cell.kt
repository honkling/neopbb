package me.honkling.neopbb.lib

import io.netty.util.internal.ThreadLocalRandom
import me.honkling.neopbb.currentPrison
import org.bukkit.Location
import org.bukkit.entity.Player

fun getRandomCell(list: List<Location>): Location {
    if(list.isEmpty())
        return currentPrison.respawn

    val random = ThreadLocalRandom.current();
    val randomCellIndex = random.nextInt(0, list.size)

    return list[randomCellIndex]
}

fun isInCell(player: Player, list: List<Location>, size: Int = 2): Boolean {
    val playerLocation = player.location

    val inCell = list.any { loc ->
        val dx = playerLocation.blockX - loc.blockX
        val dy = playerLocation.blockY - loc.blockY
        val dz = playerLocation.blockZ - loc.blockZ

        dx in -size..size && dy in -size..size && dz in -size..size && loc.world == playerLocation.world
    }

    return inCell
}