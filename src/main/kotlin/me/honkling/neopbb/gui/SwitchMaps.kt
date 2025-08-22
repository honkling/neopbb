package me.honkling.neopbb.gui

import me.honkling.neopbb.config.prisonsToml
import me.honkling.neopbb.currentPrison
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.builder
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.switchMap
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil

class SwitchMaps {
    private fun buildInventory(): Inventory {
        val size = (ceil(prisonsToml.prisons.size / 9.0).toInt() * 9).coerceIn(0, 6)
        val inventory = Bukkit.createInventory(null, size, Component.text("Switch Maps"))

        for ((index, prison) in prisonsToml.prisons.withIndex()) {
            val itemStack = ItemStack(prison.icon)
                .builder()
                .displayName(prison.name.mm)
                .build()

            inventory.setItem(index, itemStack)
        }

        return inventory
    }

    class EventNode(val inventory: Inventory, val player: Player) : Listener {
        @EventHandler
        fun onClick(event: InventoryClickEvent) {
            if (event.whoClicked != player || event.inventory != inventory)
                return

            val index = event.slot
            val prison = prisonsToml.prisons.getOrNull(index)
                ?: return

            if (prison.name == currentPrison.name) {
                player.sendMessage("<p>This map is already selected.".mm)
                return
            }

            switchMap(prison)
        }

        @EventHandler
        fun onClose(event: InventoryCloseEvent) {
            if (event.player == player && event.inventory == inventory)
                HandlerList.unregisterAll(this)
        }
    }

    fun Player.openGUI() {
        val inventory = buildInventory()
        val events = EventNode(inventory, this)
        Bukkit.getPluginManager().registerEvents(events, instance)
        openInventory(inventory)
    }
}