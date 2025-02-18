@file:Listener

package me.honkling.neopbb.event

import io.papermc.paper.event.player.PlayerOpenSignEvent
import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.config.itemsToml
import me.honkling.neopbb.lib.namespace
import me.honkling.neopbb.lib.shopPrice
import me.honkling.neopbb.profile.profile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.block.Sign
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType

val itemShopKey = NamespacedKey(namespace, "item_shop_key")

private fun buyShopItem(event: PlayerOpenSignEvent) {
    val player = event.player
    val profile = player.profile
    val sign = event.sign
    val side = event.side

    if (player.gameMode != GameMode.CREATIVE)
        event.isCancelled = true

    println("Interaction")

    if (!sign.persistentDataContainer.has(itemShopKey))
        return

    println("Has key")

    val signSide = sign.getSide(side)
    val itemKey = sign.persistentDataContainer.get(itemShopKey, PersistentDataType.STRING)!!
    println("Key: $itemKey (${itemsToml.items[itemKey]}")
    val item = itemsToml.items[itemKey]
        ?: return

    println("Price: ${item.shopPrice}")

    val shopPrice = item.shopPrice
        ?: return

    val name = item.itemMeta.displayName()!!.decorate(TextDecoration.BOLD)
    val price = Component.text("\$$shopPrice")

    if (signSide.line(2) != price) {
        println("Bad price :(")
        signSide.line(1, name)
        signSide.line(2, price)
        return
    }

    signSide.line(1, name)


    if (profile.money < shopPrice)
        return player.sendMessage(Component.text("You cannot afford this item.")
            .color(NamedTextColor.RED))

    println("yay")

    profile.money -= shopPrice
    player.inventory.addItem(item)
}

private fun onPlaceShop(event: BlockPlaceEvent) {
    val item = event.itemInHand
    val container = item.itemMeta.persistentDataContainer
    val key = container.get(itemShopKey, PersistentDataType.STRING)
        ?: return

    val state = event.block.state as Sign
    state.persistentDataContainer.set(itemShopKey, PersistentDataType.STRING, key)
    state.update()
}