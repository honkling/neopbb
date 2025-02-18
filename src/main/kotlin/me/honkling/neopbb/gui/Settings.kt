package me.honkling.neopbb.gui

import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.displayName
import me.honkling.neopbb.lib.lore
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.Profile
import me.honkling.neopbb.profile.profile
import me.honkling.pocket.GUI
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

private val enable = Component.text("Enable ")
    .decoration(TextDecoration.ITALIC, false)
private val disable = Component.text("Disable ")
    .decoration(TextDecoration.ITALIC, false)
private val settings = mapOf(
    Profile::pingNoises to ItemStack(Material.GOLD_NUGGET)
        .displayName("Ping Noises".mm)
        .lore("<gray>Plays a 'ping' noise when you are mentioned in chat.".mm),
    Profile::wardenSpaces to ItemStack(Material.SCULK)
        .displayName("Warden Spaces".mm)
        .lore("<gray>Places empty lines around the warden's chat messages.".mm),
    Profile::showGlow to ItemStack(Material.OCHRE_FROGLIGHT)
        .displayName("Show Glow".mm)
        .lore("<gray>Shows a glow effect around naughty prisoners.".mm)
)

class SettingsGUI(val player: Player) : GUI(instance, """
    x012x
""".trimIndent(), "Settings", InventoryType.HOPPER) {
    init {
        val profile = player.profile
        put('x', ItemStack(Material.BLACK_STAINED_GLASS_PANE))

        for ((index, entry) in settings.entries.withIndex()) {
            val (property, item) = entry
            var value = property.get(profile)
            val name = item.itemMeta.displayName()!!
            val clone = item.clone()
                .displayName(toggle(value).append(name))

            put(index.digitToChar(), clone) { event ->
                value = !value
                property.set(profile, value)
                clone.displayName(toggle(value).append(name))
                event.inventory.setItem(event.slot, clone)
                event.whoClicked.playSound(Sound.sound()
                    .type(Key.key("block.note_block.pling"))
                    .pitch(if (value) 1.5f else 0.8f)
                    .build())
            }
        }
    }

    private fun toggle(value: Boolean)
        = if (value) disable else enable
}