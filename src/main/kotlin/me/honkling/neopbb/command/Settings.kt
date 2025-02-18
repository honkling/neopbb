@file:Command("settings", "options")

package me.honkling.neopbb.command

import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.commando
import me.honkling.neopbb.gui.SettingsGUI
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.Profile
import me.honkling.neopbb.profile.profile
import org.bukkit.entity.Player

private val settings = mapOf(
    "ping noises" to Profile::pingNoises,
    "warden spaces" to Profile::wardenSpaces,
    "show glow" to Profile::showGlow)

private fun settings(player: Player, key: String? = null) {
    val lowercaseKey = key?.lowercase()
    val property = settings[lowercaseKey]

    if (property != null) {
        val profile = player.profile
        val newValue = !property.get(profile)
        property.set(player.profile, newValue)

        player.sendMessage("<success><good2>$lowercaseKey</good2> is now ${if (newValue) "enabled" else "disabled"}".mm)
        return
    }

    SettingsGUI(player).open(player)
}

private fun `settings$complete`(_player: Player, node: ParameterNode<Command>, input: String): List<String> {
    if (node.context.type != commando.typeRegistry[String::class])
        return emptyList()

    return settings.keys
        .filter { it.contains(input, true) }
}


