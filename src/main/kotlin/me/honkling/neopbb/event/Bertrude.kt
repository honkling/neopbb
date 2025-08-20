@file:Listener

package me.honkling.neopbb.event

import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.bertrude
import me.honkling.neopbb.gui.Bertrude
import me.honkling.neopbb.lib.mm
import net.kyori.adventure.text.Component
import org.bukkit.event.player.PlayerInteractAtEntityEvent

private fun onInteract(event: PlayerInteractAtEntityEvent) {
    val player = event.player
    val entity = event.rightClicked

    if (entity != bertrude)
        return

    event.isCancelled = true
    player.sendMessage("<s>bertrude <gray>»</s> hello i am bertrude".mm)
    val gui = Bertrude(player)
    gui.openGUI()
}