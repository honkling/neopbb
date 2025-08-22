@file:Listener

package me.honkling.neopbb.event

import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.profile.keycard
import me.honkling.neopbb.world
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.block.data.type.Door
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

private fun onInteract(event: PlayerInteractEvent) {
    val player = event.player
    val hand = event.hand ?: return
    val block = event.clickedBlock ?: return
    val state = block.state
    val data = state.blockData as? Door ?: return

    val mainItem = player.inventory.getItem(hand).asOne()
    val offItem = player.inventory.getItem(hand.oppositeHand).asOne()
    if (block.type != Material.IRON_DOOR || mainItem != keycard || (offItem == keycard && hand != EquipmentSlot.HAND))
        return

    block.world.playSound(Sound.sound {
        it.type(Key.key(
            if (data.isOpen) "minecraft:block.iron_door.close"
            else "minecraft:block.iron_door.open"
        ))
    }, block.x.toDouble(), block.y.toDouble(), block.z.toDouble())
    event.isCancelled = true
    data.isOpen = !data.isOpen
    state.blockData = data
    state.update()
}