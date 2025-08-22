@file:Listener

package me.honkling.neopbb.event

import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.prepare
import me.honkling.neopbb.profile.role
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.event.player.PlayerInteractEvent

private fun onInteract(event: PlayerInteractEvent) {
    val player = event.player
    val state = event.clickedBlock?.state as? Sign
        ?: return

    val side = state.getSide(Side.FRONT)
    val line = PlainTextComponentSerializer.plainText().serialize(side.line(1))

    when (line) {
        "Get Gear" -> {
            if (player.role != Role.Prisoner) {
                return player.sendMessage("<p>Only prisoners can escape.".mm)
            }

            player.role = Role.Criminal
            player.prepare(false, broadcast = true)
        }
        "Restore Kit" -> {
            if (player.role == Role.Warden || !player.role.isAuthority)
                return player.sendMessage("<p>Only guards, nurses, and swats can restore their kit.".mm)

            player.prepare(true)
        }
    }
}