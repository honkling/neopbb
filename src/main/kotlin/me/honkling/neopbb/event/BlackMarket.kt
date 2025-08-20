@file:Listener

package me.honkling.neopbb.event

import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.currentPrison
import me.honkling.neopbb.lib.*
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.isInBlackMarket
import me.honkling.neopbb.profile.purchaseItem
import me.honkling.neopbb.profile.role
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.TitlePart
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

private fun onInteract(event: PlayerInteractEvent) {
    val player = event.player
    val block = event.clickedBlock
        ?: return

    if (block.type == Material.CAULDRON) {
        if (player.role.isAuthority && player.role != Role.Warden)
            return player.sendMessage("<p>You can't go in there..!".mm)

        if (player.passengers.isNotEmpty())
            return player.sendMessage("<p>You can't go in the black market while somebody is handcuffed.".mm)

        player.isInBlackMarket = true
        player.teleport(currentPrison.blackMarketIn)
        player.sendTitlePart(TitlePart.TITLE, "<gray>-= Black Market =-".mm)
        player.playSound(Sound.sound {
            it.type(Key.key("minecraft:ambient.underwater.enter"))
            it.pitch(0.75f)
        })
        return
    }

    val state = block.state as? Sign
        ?: return

    val side = state.getSide(Side.FRONT)
    val lineOne = PlainTextComponentSerializer.plainText().serialize(side.line(1))
    val lineTwo = PlainTextComponentSerializer.plainText().serialize(side.line(2))

    if (lineOne == "Leave Market") {
        player.isInBlackMarket = false
        player.teleport(currentPrison.blackMarketOut)
        player.playSound(Sound.sound {
            it.type(Key.key("minecraft:entity.ender_pearl.throw"))
        })
        return
    }

    when (lineTwo) {
        "Dagger" -> player.purchaseItem(400f, dagger)
        "Scrap Metal" -> player.purchaseItem(150f, scrapMetal)
        "Supreme Stick" -> player.purchaseItem(50f, supremeStick)
        "Illegal Healing" -> player.purchaseItem(30f, illegalGoldenApple)
        "Coal" -> player.purchaseItem(30f, coal)
        "Arrows" -> player.purchaseItem(16f, ItemStack(Material.ARROW, 8))
        "Strong Chest" -> player.purchaseItem(1000f, ItemStack(Material.IRON_CHESTPLATE))
    }
}