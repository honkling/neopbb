package me.honkling.neopbb.gui.admin

import me.honkling.pocket.CloseHandler
import me.honkling.pocket.GUI
import me.honkling.neopbb.config.itemsToml
import me.honkling.neopbb.event.itemShopKey
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.lib.shopPrice
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.persistence.PersistentDataType

private const val itemsPerPage = 7 * 4

open class ItemsGUI(val shopMode: Boolean = false) : GUI(instance, """
    xxxxxxxxx
    xiiiiiiix
    xiiiiiiix
    xiiiiiiix
    xiiiiiiix
    xxlxcxrxx
""".trimIndent(), if (shopMode) "Shop Items" else "Items") {
    class SignPicker(val item: ItemStack) : GUI(instance, """
        xxxxxxxxx
        xxxxxxxxx
        xx0000000
        xx0000000
    """.trimIndent(), "Pick Sign") {
        init {
            val materials = Material.entries
            val signs = materials.filter {
                it.name.endsWith("_SIGN") &&
                !it.name.endsWith("HANGING_SIGN") &&
                !it.name.endsWith("WALL_SIGN")
            }
            val hangingSigns = materials.filter { it.name.endsWith("_HANGING_SIGN") }

            for ((index, sign) in signs.withIndex()) {
                val slot = index / 9 * 18 + index.mod(9)
                val hangingSign = hangingSigns[index]

                put(slot, ItemStack(sign), ::handleClick)
                put(slot + 9, ItemStack(hangingSign), ::handleClick)
            }
        }

        private fun handleClick(event: InventoryClickEvent) {
            val sign = event.currentItem
                ?: return

            sign.editMeta { meta ->
                meta as BlockStateMeta
                val key = itemsToml.items.entries.find { it.value == item }!!.key
                meta.persistentDataContainer.set(itemShopKey, PersistentDataType.STRING, key)
                meta.displayName(item.displayName()
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))

                val blockState = meta.blockState as Sign
                val frontSide = blockState.getSide(Side.FRONT)

                frontSide.line(1, item.itemMeta.displayName()!!
                    .decorate(TextDecoration.BOLD))
                frontSide.line(2, Component.text("\$${item.shopPrice!!}"))

                meta.blockState = blockState
            }

            event.whoClicked.inventory.addItem(sign)
            event.inventory.close()
        }
    }

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

            if (shopMode) {
                val gui = SignPicker(item)
                gui.open(it.whoClicked as Player)
                return@put
            }

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

        put('r', if (page < pages) next else pane) {
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

    override fun buildInventory(): Inventory {
        val inventory = super.buildInventory()
        render(inventory)
        return inventory
    }

    private fun render(inventory: Inventory) {
        val entries = itemsToml.items.values.toList()
            .filter { (!shopMode || it.shopPrice != null) && !it.isEmpty }
        var slot = 10
        var i = 0

        while (slot < 44) {
            if ((slot + 1).mod(9) == 0)
                slot += 2

            val itemIndex = page * itemsPerPage + i
            val item =
                if (itemIndex >= entries.size) emptySlot
                else entries[itemIndex]

//            if (item.isEmpty) {
//                i++
//                continue
//            }

            inventory.setItem(slot, item)

            slot++
            i++
        }
    }
}