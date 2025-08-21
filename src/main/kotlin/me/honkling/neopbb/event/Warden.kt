@file:Listener

package me.honkling.neopbb.event

import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.gui.SwitchMaps
import me.honkling.neopbb.lastLockdown
import me.honkling.neopbb.lastMapSwitch
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.*
import me.honkling.neopbb.schedule.Period
import me.honkling.neopbb.schedule.period
import me.honkling.neopbb.schedule.tickSchedule
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
private fun onInteract(event: PlayerInteractEvent) {
    val player = event.player
    val state = event.clickedBlock?.state as? Sign
        ?: return

    val side = state.getSide(Side.FRONT)
    val lineOne = PlainTextComponentSerializer.plainText().serialize(side.line(1))
    val lineTwo = PlainTextComponentSerializer.plainText().serialize(side.line(2))

    if (lineTwo == "SWAT Guards")
        player.purchase(2500f, swatUnlocked) {
            Bukkit.getServer().sendMessage("<p><s>${player.name}</s> has unlocked SWAT guards!".mm)
            swatUnlocked = true
        }
    else when (lineOne) {
        "Lockdown" -> {
            val since = Clock.System.now().epochSeconds - lastLockdown
            val cooldown = 60 * 10

            if (since < cooldown) {
                val remaining = cooldown - since
                val unit = if (remaining == 1L) "second" else "seconds"
                return player.sendMessage("<p>That's on cooldown! <s>$remaining $unit</s> left.".mm)
            }

            period = Period.Lockdown
            lastLockdown = Clock.System.now().epochSeconds
            tickSchedule()
        }
        "Switch Maps" -> {
            if (player != warden)
                return player.sendMessage("<p>Only the warden can switch maps.".mm)

            val since = Clock.System.now().epochSeconds - lastMapSwitch
            val cooldown = 60 * 3

            if (since < cooldown) {
                val remaining = cooldown - since
                val unit = if (remaining == 1L) "second" else "seconds"
                return player.sendMessage("<p>That's on cooldown! <s>$remaining $unit</s> left.".mm)
            }

            val gui = SwitchMaps()

            with (gui) {
                player.openGUI()
            }
        }
    }
}

private fun onOpenEnderChest(event: PlayerInteractEvent) {
    val player = event.player

    if (event.clickedBlock?.type == Material.ENDER_CHEST && event.action.isRightClick && player.role.isAuthority) {
        val rolePart = if (player.role == Role.Warden) "the warden" else "a ${player.role.name.lowercase()}"

        player.sendMessage("<p>You cannot open your ender chest as $rolePart.".mm)
        event.isCancelled = true
    }
}