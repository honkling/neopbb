@file:Listener

package me.honkling.neopbb.event.item

import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.lib.key
import org.bukkit.block.data.type.Door
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

private fun onInteract(event: PlayerInteractEvent) {
    val player = event.player
    val inventory = player.inventory
    val hand = event.hand ?: return
    val block = event.clickedBlock
        ?: return

    val type = block.type.name
    val otherHand = hand.oppositeHand
    if (("DOOR" !in type || "TRAPDOOR" in type) || inventory.getItem(hand).asQuantity(1) != key)
        return

    if (inventory.getItem(otherHand).asQuantity(1) != key || hand == EquipmentSlot.HAND) {
        event.isCancelled = true
        val state = block.state
        val door = state.blockData as Door
        door.isOpen = !door.isOpen
        state.blockData = door
        state.update()
    }
}