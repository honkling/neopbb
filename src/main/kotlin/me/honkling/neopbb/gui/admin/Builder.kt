package me.honkling.neopbb.gui.admin

import me.honkling.pocket.GUI
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.mm
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

interface BuilderGUI {
    class Menu : GUI(instance,  """
        00s00
    """.trimIndent(), "Builder", InventoryType.HOPPER) {
        init {
            val shopItem = ItemStack.of(Material.EMERALD)
            shopItem.editMeta {
                it.displayName(Component.text("Shop Items")
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
            }

            put('s', shopItem) {
                Shop().open(it.whoClicked as Player)
            }
        }
    }

    class Shop : ItemsGUI(true) {
        init {
            val close = ItemStack.of(Material.BARRIER)
            close.editMeta {
                it.displayName("Close".mm)
            }

            put('c', close) {
                BuilderGUI().open(it.whoClicked as Player)
            }
        }
    }
}

@Suppress("FunctionName")
fun BuilderGUI() = BuilderGUI.Menu()