package me.honkling.neopbb.lib

import me.honkling.neopbb.config.PrisonsToml.Prison
import me.honkling.neopbb.currentPrison
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.reflect.KProperty1

enum class CellType(val property: KProperty1<Prison, List<Location>>) {
    Prisoner(Prison::prisonerCells),
    Solitary(Prison::solitaryCells)
}

fun getRandomCell(cells: List<Location>): Location {
    if (cells.isEmpty())
        return currentPrison.respawn

    return cells[(Math.random() * cells.size).toInt()]
}

fun isInCell(player: Player, cells: List<Location>, size: Int = 2): Boolean {
    val location = player.location

    return cells.any {
        val dx = location.blockX - it.blockX
        val dy = location.blockY - it.blockY
        val dz = location.blockZ - it.blockZ

        dx in -size..size && dy in -size..size && dz in -size..size && it.world == location.world
    }
}