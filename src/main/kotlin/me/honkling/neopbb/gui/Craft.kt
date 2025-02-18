package me.honkling.neopbb.gui

import me.honkling.neopbb.config.itemsToml
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.*
import me.honkling.pocket.CloseHandler
import me.honkling.pocket.GUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.math.min

private val pane = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)
private val back = ItemStack.of(Material.ARROW)
    .displayName("Back".mm)
private val close = ItemStack.of(Material.BARRIER)
    .displayName("Close".mm)

val recipeTypeKey = NamespacedKey(namespace, "recipe_type")
val recipeKey = NamespacedKey(namespace, "recipe")

interface CraftGUI {
    class SelectRecipe : GUI(instance, """
        xxxxxxxxx
        xxaaaaaxx
        xxaaaaaxx
        xxbxcxnxx
    """.trimIndent(), "Select Recipe"), CraftGUI {
        private val itemsPerPage = 10
        private val backSlot = 29
        private val next = ItemStack.of(Material.ARROW)
            .displayName("Next".mm)
        private val empty = ItemStack.of(Material.STONE_BUTTON)
            .displayName(Component.empty())
        private var page = 0
        private val items = itemsToml.items.values.toList()
            .filter { it.persistentDataContainer.has(recipeKey) }
        private val pages = items.size / itemsPerPage

        init {
            put('x', pane)
            put('c', close) {
                it.inventory.close()
            }

            put('a', empty) {
                val item = it.currentItem

                if (item == null || item == empty)
                    return@put

                val player = it.whoClicked as Player
                val gui = CraftRecipe(player, item)
                gui.open(player)
            }

            put('b', pane) {
                val inventory = it.inventory
                page--

                if (it.currentItem != back)
                    return@put

                if (page == 0)
                    inventory.setItem(it.slot, pane)

                render(inventory)
            }

            put('n', if (page < pages) next else pane) {
                if (it.currentItem != next)
                    return@put

                val inventory = it.inventory
                inventory.setItem(backSlot, back)

                page++

                if (page >= pages)
                    inventory.setItem(it.slot, pane)

                render(inventory)
            }
        }

        override fun buildInventory(): Inventory {
            val inventory = super.buildInventory()
            render(inventory)
            return inventory
        }

        private fun render(inventory: Inventory) {
            val thisPage = items.subList(page * itemsPerPage, min(items.size, page * itemsPerPage + itemsPerPage))
            var finalIndex = 0

            println(items)

            for ((index, item) in thisPage.withIndex()) {
                val slot = getRecipeSlot(index)

                inventory.setItem(slot, item)
                finalIndex = index
            }

            for (index in finalIndex + 1..<9) {
                val slot = getRecipeSlot(index)
                inventory.setItem(slot, empty)
            }
        }

        private fun getRecipeSlot(index: Int): Int {
            val row = 9
            val recipe = 5
            return index / recipe * row + row + 2 + index.mod(recipe)
        }
    }

    class CraftRecipe(val player: Player, val item: ItemStack) : GUI(instance, """
        xxxxxxxxx
        xxxxxxxxx
        xxbxmxcxx
    """.trimIndent(), "Craft ${item.displayName().plainText()}") {
        private val isPlaceholderKey = NamespacedKey(namespace, "is_placeholder")
        private val placedItems = mutableListOf<ItemStack>()
        private val recipe = item.recipe!!
        private val createSlot = 22

        init {
            val row = 9
            val maxSize = 7
            val size = recipe.size
            val padding = (maxSize - size) / 2.0

            for ((index, item) in recipe.withIndex()) {
                val slot = index + padding.toInt() + row + 1 +
                        if (index >= size / 2 && padding.mod(1.0) != 0.0) 1 else 0

                val displayItem = displayItem(item)

                put(slot, displayItem) { event ->
                    val currentItem = event.currentItem
                        ?: return@put

                    if (currentItem == pane)
                        return@put

                    val cursor = event.cursor

                    if (currentItem.itemType == cursor.itemType) {
                        if (currentItem.asOne() == cursor.asOne()) {
                            when {
                                event.isLeftClick -> {
                                    event.currentItem = currentItem.asQuantity(currentItem.amount + cursor.amount)
                                    event.whoClicked.setItemOnCursor(null)
                                }
                                event.isRightClick -> {
                                    event.currentItem = currentItem.asQuantity(currentItem.amount + 1)
                                    event.whoClicked.setItemOnCursor(if (cursor.amount == 1) null
                                        else cursor.asQuantity(cursor.amount - 1))
                                }
                                else -> return@put
                            }

                            val placedItem = placedItems.find { it.itemType == cursor.itemType }!!
                            placedItem.amount = event.currentItem!!.amount
                            return@put
                        }

                        when {
                            event.isLeftClick -> {
                                event.currentItem = cursor
                                event.whoClicked.setItemOnCursor(null)
                            }
                            event.isRightClick -> {
                                event.currentItem = cursor.asOne()
                                event.whoClicked.setItemOnCursor(if (cursor.amount == 1) null
                                    else cursor.asQuantity(cursor.amount - 1))
                            }
                            else -> return@put
                        }

                        placedItems += event.currentItem!!

                        if (placedItems.size == recipe.size)
                            event.inventory.setItem(createSlot, ItemStack.of(Material.ANVIL)
                                .displayName("Complete Craft".mm))

                        return@put
                    } else if (currentItem.persistentDataContainer.get(
                            isPlaceholderKey,
                            PersistentDataType.BOOLEAN
                        ) != true) {
                        event.whoClicked.setItemOnCursor(currentItem)
                        event.currentItem = displayItem(currentItem.asOne())
                        event.inventory.setItem(createSlot, pane)
                        placedItems -= currentItem
                    }
                }
            }

            put('m', pane) {
                if (it.currentItem == pane)
                    return@put

                it.whoClicked.inventory.addItem(item)
                val removedItems = mutableListOf<ItemStack>()

                for (item in placedItems) {
                    val index = recipe.indexOf(item.asOne())
                    val slot = index + padding.toInt() + row + 1 +
                            if (index >= size / 2 && padding.mod(1.0) != 0.0) 1 else 0

                    if (item.amount == 1) {
                        it.inventory.setItem(slot, displayItem(item))
                        it.inventory.setItem(createSlot, pane)
                        removedItems += item
                    } else {
                        item.amount--
                        it.inventory.setItem(slot, item)
                    }
                }

                for (item in removedItems)
                    placedItems -= item
            }

            put('x', pane)
            put('c', close) {
                it.inventory.close()
                refundItems()
            }

            put('b', back) {
                SelectRecipe().open(it.whoClicked as Player)
                refundItems()
            }
        }

        override fun open(player: Player, onClose: CloseHandler) {
            super.open(player) {
                refundItems()
                onClose(it)
            }
        }

        private fun displayItem(item: ItemStack): ItemStack {
            val displayItem = item.clone()

            displayItem.editMeta {
                it.setEnchantmentGlintOverride(true)
                it.displayName(Component.text("Place ")
                    .decoration(TextDecoration.ITALIC, false)
                    .append(displayItem.displayName()
                        .decoration(TextDecoration.ITALIC, false))
                    .append(Component
                        .text(" Here")))

                it.persistentDataContainer.set(isPlaceholderKey, PersistentDataType.BOOLEAN, true)
            }

            return displayItem
        }

        private fun refundItems() {
            for (item in placedItems)
                player.inventory.addItem(item)

            placedItems.clear()
        }
    }
}




class OldCraftGUI : GUI(instance, """
    xxbxxxxxx
    xaaaxcccx
    xaaaxcccr
    xaaaxcccx
    xxnxxxxxx
""".trimIndent(), "Crafting") {
    private val itemsPerPage = 9
    private val backSlot = 2
    private val nextSlot = 38
    private val resultSlot = 26
    private val pane = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)
    private val back = ItemStack.of(Material.ARROW)
        .displayName("Back".mm)
    private val next = ItemStack.of(Material.ARROW)
        .displayName("Next".mm)
    private val empty = ItemStack.of(Material.STONE_BUTTON)
        .displayName(Component.empty())
    private var page = 0
    private val items = itemsToml.items.values.toList()
        .filter { it.persistentDataContainer.has(recipeTypeKey) }
    private val pages = items.size / itemsPerPage

    init {
        val noResult = ItemStack.of(Material.BARRIER)
            .displayName("<red>Invalid Recipe".mm)

        put('x', pane)
        put('c', ItemStack.of(Material.AIR)) {
            it.isCancelled = false
        }

        put('a', empty) {
            val inventory = it.inventory
            val item = it.currentItem

            if (item == null || item == empty)
                return@put

            val recipe = item.recipe
                ?: return@put

            for (index in 0..<9) {
                val slot = getRecipeSlot(index)
                inventory.setItem(slot, ItemStack.empty())
            }

            for ((index, itemStack) in recipe.withIndex()) {
                val slot = getRecipeSlot(index)
                inventory.setItem(slot, itemStack)
            }

            page++
            inventory.setItem(backSlot, back)
            inventory.setItem(nextSlot, pane)
        }

        put('b', pane) {
            val inventory = it.inventory
            page--

            if (it.currentItem != back)
                return@put

            if (page == 0)
                inventory.setItem(it.slot, pane)

            render(inventory)
        }

        put('n', if (page < pages) next else pane) {
            if (it.currentItem != next)
                return@put

            val inventory = it.inventory
            inventory.setItem(backSlot, back)

            page++

            if (page >= pages)
                inventory.setItem(it.slot, pane)

            render(inventory)
        }

        put('r', noResult) {
            val result = it.currentItem

            if (result?.equals(noResult) != false)
                return@put

            for (i in 0..<9)
                it.inventory.setItem(getRecipeSlot(i), ItemStack.empty())

            it.inventory.setItem(resultSlot, noResult)
            it.whoClicked.inventory.addItem(result)
        }
    }

    override fun open(player: Player, onClose: CloseHandler) {
        super.open(player, onClose)
        render(player.openInventory.topInventory)
    }

    private fun render(inventory: Inventory) {
        val thisPage = items.subList(page * itemsPerPage, min(items.size, page * itemsPerPage + itemsPerPage))
        var finalIndex = 0

        for ((index, item) in thisPage.withIndex()) {
            val slot = getRecipeSlot(index)

            inventory.setItem(slot, item)
            finalIndex = index
        }

        for (index in finalIndex + 1..<9) {
            val slot = getRecipeSlot(index)
            inventory.setItem(slot, empty)
        }
    }

    private fun getRecipeSlot(index: Int): Int {
        val row = 9
        val recipe = 3
        return index / recipe * row + row + 1 + index.mod(recipe)
    }
}