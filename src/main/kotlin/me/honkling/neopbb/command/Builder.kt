@file:Command("builder", permission = "pbb.builder")

package me.honkling.neopbb.command

import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.gui.admin.BuilderGUI
import org.bukkit.entity.Player

private fun builder(sender: Player) {
    BuilderGUI().open(sender)
}