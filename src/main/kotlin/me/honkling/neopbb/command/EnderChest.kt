@file:Command("enderchest", permission = "neopbb.enderchest")

package me.honkling.neopbb.command

import kotlinx.coroutines.launch
import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.offline.DataAccess
import me.honkling.neopbb.profile.offline.OfflineEnderChest
import me.honkling.neopbb.scope
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.entity.Player

private fun inspect(sender: Player, name: String) {
    scope.launch {
        val player = Bukkit.getOfflinePlayer(name)
        val enderChest = OfflineEnderChest(player)

        Bukkit.getScheduler().runTask(instance, Runnable {
            sender.openInventory(CraftInventory(enderChest))
        })
    }
}

private fun clear(sender: Player, name: String) {
    scope.launch {
        val player = Bukkit.getOfflinePlayer(name)
        val dataAccess = DataAccess(player)

        dataAccess.compound.put("EnderItems", ListTag(mutableListOf(), CompoundTag.TAG_COMPOUND))
        dataAccess.save()
        sender.sendMessage("<p>Cleared <s>${player.name}</s>'s ender chest.".mm)
    }
}