@file:Command("neopbb", permission = "neopbb.admin")

package me.honkling.neopbb.command

import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.config.reloadConfigToml
import me.honkling.neopbb.config.reloadFilterToml
import me.honkling.neopbb.config.reloadPrisonsToml
import me.honkling.neopbb.lib.mm
import org.bukkit.command.CommandSender

private fun reloadConfig(sender: CommandSender, config: String) {
    when (config) {
        "main" -> {
            reloadConfigToml()
            sender.sendMessage("<p>Reloaded the main configuration.".mm)
        }
        "filter" -> {
            reloadFilterToml()
            sender.sendMessage("<p>Reloaded the filter configuration.".mm)
        }
        "prisons" -> {
            reloadPrisonsToml()
            sender.sendMessage("<p>Reloaded the prisons configuration.".mm)
        }
        else -> {
            sender.sendMessage("<p>Unknown configuration.".mm)
        }
    }
}

private fun `reloadConfig$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return listOf("filter", "prisons", "main").filter { it.contains(input, true) }
}