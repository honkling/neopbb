package me.honkling.neopbb.gui

import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.builder
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.spawnWithUniform
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KMutableProperty1

val settings = mutableListOf(
    Setting(
        Material.LEATHER_HELMET,
        "Spawn with Uniform",
        "Depicts if you should spawn with your prisoner uniform.",
        Player::spawnWithUniform
    )
)

data class Setting(
    val material: Material,
    val name: String,
    val description: String,
    val property: KMutableProperty1<Player, Boolean>
)

class Bertrude(val player: Player) {
    val inventory = Bukkit.createInventory(
        null,
        9,
        Component.text("bertrude")
    )

    class EventNode(val gui: Bertrude, val player: Player) : Listener {
        @EventHandler
        fun onClick(event: InventoryClickEvent) {
            if (event.whoClicked != player || event.inventory != gui.inventory)
                return

            val index = event.slot
            val setting = settings.getOrNull(index)
                ?: return

            event.isCancelled = true
            val property = setting.property
            property.set(player, !property.get(player))
            gui.setSetting(setting)
            player.playSound(Sound.sound {
                it.type(Key.key("entity.villager.yes"))
            })
        }

        @EventHandler
        fun onClose(event: InventoryCloseEvent) {
            if (event.player == player && event.inventory == gui.inventory)
                HandlerList.unregisterAll(this)
        }
    }

    init {
        for (setting in settings)
            setSetting(setting)
    }

    fun setSetting(setting: Setting) {
        val index = settings.indexOf(setting)
        val value = setting.property.get(player)
        val valueMarker = if (value) "<s>✔ Enabled" else "<s>✖ Disabled"
        val itemStack = ItemStack(setting.material)
            .builder()
            .displayName(setting.name.mm)
            .lore(valueMarker.mm, setting.description.mm)
            .build()

        inventory.setItem(index, itemStack)
    }

    fun openGUI() {
        val events = EventNode(this@Bertrude, player)
        Bukkit.getPluginManager().registerEvents(events, instance)
        player.openInventory(inventory)
    }
}