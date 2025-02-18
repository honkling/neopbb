@file:Command("config", permission = "pbb.config")

package me.honkling.neopbb.command

import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.config.Configs
import me.honkling.neopbb.lib.mm
import org.bukkit.command.CommandSender

fun config(sender: CommandSender, config: Configs) {
    config.reload()
    sender.sendMessage("<success>Reloaded <good2>${config.fileName}</good2>.".mm)
}