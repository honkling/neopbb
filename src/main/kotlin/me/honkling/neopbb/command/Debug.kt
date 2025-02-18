@file:Command("debug", permission = "pbb.debug")

package me.honkling.neopbb.command

import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.gui.CraftGUI
import me.honkling.neopbb.gui.admin.ItemsGUI
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.profile
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

private fun role(sender: CommandSender, player: Player, role: Role? = null) {
    if (role == null) {
        val currentRole = player.profile.role
        sender.sendMessage("<info><useful2>${player.name}</useful2> is currently a <useful2>$currentRole</useful2>.".mm)
        return
    }

    player.profile.role = role
    sender.sendMessage("<success><good2>${player.name}</good2> is now a <good2>$role</good2>.".mm)
}

private fun item(sender: Player, item: ItemStack? = null) {
    if (item != null) {
        sender.inventory.addItem(item)
        sender.sendMessage("<success>You've been given the item.".mm)
        return
    }

    val gui = ItemsGUI()
    gui.open(sender)
}

private fun craft(sender: Player) {
    val gui = CraftGUI.SelectRecipe()
    gui.open(sender)
}