@file:Command("setmoney", permission = "pbb.setmoney")

package me.honkling.neopbb.command

import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.profile
import org.bukkit.entity.Player

private fun setmoney(sender: Player, target: Player, value: Double) {
    val profile = target.profile
    profile.money = value
    sender.sendMessage("<success>Set <good2>${target.name}</good2>'s balance to <good2>${profile.displayMoney()}</good2>.".mm)
}