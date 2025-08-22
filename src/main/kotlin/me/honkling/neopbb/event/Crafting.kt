@file:Listener

package me.honkling.neopbb.event

import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.gui.Crafting
import org.bukkit.Material
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

private fun onInteract(event: PlayerInteractEvent) {
    val player = event.player

    if (event.clickedBlock?.type != Material.CRAFTING_TABLE)
        return

    event.isCancelled = true
    val gui = Crafting()

    with (gui) {
        player.openGUI()
    }
}

private fun onCraft(event: CraftItemEvent) {
    event.isCancelled = true
}