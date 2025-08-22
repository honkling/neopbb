@file:Listener

package me.honkling.neopbb.event

import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.*
import me.honkling.neopbb.profile.money
import me.honkling.neopbb.profile.role
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

private val yes = Sound.sound {
    it.type(Key.key("block.note_block.basedrum"))
    it.volume(0.75f)
    it.pitch(1.75f)
}

val miningOres = mutableMapOf(
    Material.DEEPSLATE_COPPER_ORE to 7.5f,
    Material.COPPER_ORE to 7.5f,
    Material.DEEPSLATE_EMERALD_ORE to 45.0f,
    Material.EMERALD_ORE to 45.0f,
    Material.DEEPSLATE_GOLD_ORE to 25.0f,
    Material.GOLD_ORE to 25.0f,
    Material.DEEPSLATE_LAPIS_ORE to 15.0f,
    Material.LAPIS_ORE to 15.0f,
    Material.DEEPSLATE_IRON_ORE to 15.0f,
    Material.IRON_ORE to 15.0f,
    Material.DEEPSLATE_REDSTONE_ORE to 10.0f,
    Material.REDSTONE_ORE to 10.0f
)

val lumberLogs = Material.entries.filter { "LOG" in it.name && "STRIPPED_" !in it.name }

private fun onDamage(event: EntityDamageEvent) {
    val player = event.entity as? Player ?: return
    val damager = (event as? EntityDamageByEntityEvent)?.damager as? Player ?: return
    val item = damager.inventory.itemInMainHand

    if (item.compareWithoutDurability(lumberAxe) || item.compareWithoutDurability(miningPickaxe)
        || item.compareWithoutDurability(shovel)) {
        damager.sendMessage("<p>You can't use that job item to fight people!".mm)
        event.isCancelled = true
    }

    if (item.compareWithoutDurability(bountyHunterSword) && player.role.isAuthority) {
        event.isCancelled = true
        damager.sendMessage("<p>You can't use job items to hurt guards!".mm)
    }
}

private fun onBreak(event: BlockBreakEvent) {
    val player = event.player
    val itemStack = player.inventory.itemInMainHand
    val block = event.block
    val type = block.type

    if (itemStack.compareWithoutDurability(lumberAxe) && type in lumberLogs) {
        val data = block.blockData
        block.type = Material.valueOf("STRIPPED_${block.type.name}")
        player.sendMessage("<s>+$2</s> for cutting wood".mm)
        player.playSound(yes)
        player.money += 2

        if (Math.random() <= 0.2) {
            player.sendMessage("<p>You extracted a plank from the log! (<s>20%</s>)".mm)
            player.give(lumber)
            return
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, {
            block.world.setBlockData(block.location, data)
        }, 20L * 4)
    }

    if (itemStack.compareWithoutDurability(miningPickaxe) && type in miningOres) {
        block.type = if ("DEEPSLATE" in type.name) Material.COBBLED_DEEPSLATE else Material.COBBLESTONE
        val amount = miningOres[type]!!
        player.sendMessage("<s>+$$amount</s> for mining ores".mm)
        player.playSound(yes)
        player.money += amount

        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, {
            block.type = type
        }, 20L * 8)
    }

    if (itemStack.compareWithoutDurability(shovel) && type == Material.COARSE_DIRT) {
        block.type = Material.DIRT
        player.sendMessage("<s>+$8</s> for shovelling dirt".mm)
        player.playSound(yes)
        player.money += 8

        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, {
            block.type = Material.COARSE_DIRT
        }, 20L * 10)
    }
}

private fun onDeath(event: PlayerDeathEvent) {
    val attacker = event.damageSource.causingEntity as? Player
        ?: return

    if (attacker.inventory.itemInMainHand.compareWithoutDurability(bountyHunterSword)) {
        attacker.money += 100
        attacker.sendMessage("<s>+$100</s> for killing somebody".mm)
        attacker.playSound(yes)
    }
}

private fun onInteract(event: PlayerInteractEvent) {
    val player = event.player
    val hand = event.hand ?: return
    val block = event.clickedBlock ?: return

    if (block.type == Material.TRAPPED_CHEST) {
        event.isCancelled = true
        player.inventory.addItem(ItemStack(Material.COD))
    }

    if (block.type == Material.BLAST_FURNACE)
        event.isCancelled = true

    if (block.type == Material.BLAST_FURNACE && event.item?.type == Material.COD && player.getCooldown(Material.COD) <= 0) {
        event.item!!.amount--
        player.setCooldown(Material.COD, 2)
        player.playSound(yes)

        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, {
            player.playSound(yes)
            player.money += 2
        }, 20L * 4)
    }

    val state = block.state as? Sign
    if (state != null) {
        val side = state.getSide(Side.FRONT)
        val line = PlainTextComponentSerializer.plainText().serialize(side.line(2))

        player.inventory.addItem(when (line) {
            "Lumberjack" -> lumberAxe
            "Plumber" -> plumbingStick
            "Bounty Hunter" -> bountyHunterSword
            "Shovelling" -> shovel
            "Mining" -> miningPickaxe
            else -> return
        })
        return
    }

    val mainItem = player.inventory.getItem(hand).asOne()
    val offItem = player.inventory.getItem(hand.oppositeHand).asOne()
    if (!mainItem.compareWithoutDurability(plumbingStick)
        || (offItem.compareWithoutDurability(plumbingStick) && hand != EquipmentSlot.HAND)
        || block.type != Material.IRON_TRAPDOOR || player.getCooldown(Material.CARROT_ON_A_STICK) > 0)
        return

    player.money += 2.5f
    player.setCooldown(Material.CARROT_ON_A_STICK, 4)
    player.playSound(yes)
}