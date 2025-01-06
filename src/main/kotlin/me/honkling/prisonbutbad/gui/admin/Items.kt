package me.honkling.prisonbutbad.gui.admin

import me.honkling.pocket.CloseHandler
import me.honkling.pocket.GUI
import me.honkling.prisonbutbad.config.itemsToml
import me.honkling.prisonbutbad.instance
import me.honkling.prisonbutbad.lib.mm
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

private const val itemsPerPage = 7 * 4

class ItemsGUI : GUI(instance, """
    xxxxxxxxx
    xiiiiiiix
    xiiiiiiix
    xiiiiiiix
    xiiiiiiix
    xxlxcxrxx
""".trimIndent(), "Items") {
    private val pages = itemsToml.items.size / itemsPerPage
    private var page = 0
    private val pane = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)
    private val plainTemplate = template.replace("\n", "")
    private val backIndex = plainTemplate.indexOf('l')
    private val nextIndex = plainTemplate.indexOf('r')
    private val emptySlot = ItemStack.of(Material.STONE_BUTTON)
        .also { item ->
            item.editMeta {
                it.displayName(Component.empty())
            }
        }

    private val back = ItemStack.of(Material.ARROW)
        .also { item ->
            item.editMeta {
                it.displayName("Previous Page".mm)
            }
        }

    private val next = ItemStack.of(Material.ARROW)
        .also { item ->
            item.editMeta {
                it.displayName("Next Page".mm)
            }
        }

    init {
        val close = ItemStack.of(Material.BARRIER)
        close.editMeta {
            it.displayName("Close".mm)
        }

        put('x', pane)
        put('i', emptySlot) {
            val inventory = it.inventory
            val item = inventory.getItem(it.slot)

            if (item == null || item == emptySlot)
                return@put

            it.whoClicked.inventory.addItem(item)
        }

        put('l', pane) {
            val slot = it.slot
            val inventory = it.inventory
            val item = inventory.getItem(slot)

            if (item != back)
                return@put

            page--

            if (page <= 0)
                inventory.setItem(slot, pane)

            if (page < pages)
                inventory.setItem(nextIndex, next)

            render(inventory)
        }

        put('r', if (page < pages) pane else next) {
            val slot = it.slot
            val inventory = it.inventory
            val item = inventory.getItem(slot)

            if (item != back)
                return@put

            page++

            inventory.setItem(backIndex, back)
            if (page >= pages)
                inventory.setItem(slot, pane)

            render(inventory)
        }

        put('c', close) {
            it.inventory.close()
        }
    }

    override fun open(player: Player, onClose: CloseHandler) {
        super.open(player, onClose)
        render(player.openInventory.topInventory)
    }

    private fun render(inventory: Inventory) {
        val entries = itemsToml.items.values.toList()
        var slot = 0
        var i = 0

        while (slot < 44) {
            if ((slot + 1).mod(9) == 0)
                slot += 2

            val itemIndex = page * itemsPerPage + i
            val item =
                if (itemIndex >= entries.size) emptySlot
                else entries[itemIndex]

            inventory.setItem(slot, item)

            slot++
            i++
        }
    }
}