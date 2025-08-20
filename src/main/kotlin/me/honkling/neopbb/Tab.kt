package me.honkling.neopbb

import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun refreshTab() {
    val players = Bukkit.getOnlinePlayers()
    val prisoners = players.filter { !it.role.isAuthority } .sortedBy { it.role.ordinal }
    val guards = players.filter { it.role.isAuthority && it.role != Role.Warden } .sortedBy { it.role.ordinal }

    val noDamage = if ((warden?.noDamageTicks ?: -1) > 0)
        " <aqua>[${warden!!.noDamageTicks / 20}s of No-Damage]</aqua>"
    else ""

    val now = Clock.System.now().epochSeconds
    val wardenDisplay = if (warden == null)
        Component.text("No warden!")
    else displayPlayer(warden!!)
        .appendNewline()
        .append("<red>(${warden!!.health.toInt()} HP$noDamage) for ${(now - wardenStart) / 60}m".mm)

    val guardDisplay = Component.join(
        JoinConfiguration.newlines(),
        guards.map(::displayPlayer)
    )

    val prisonerDisplay = Component.join(
        JoinConfiguration.newlines(),
        prisoners.map(::displayPlayer)
    )

    Bukkit.getServer().sendPlayerListHeader("""
        <gray>---
        <s>neopbb</s> - <white>made by rosalyn!</white>
        <green>Players: ${players.size}</green>
        <red><warden></red>
        ---
        
        <aqua>Guards (${guards.size}):</aqua>
        
        <guards>
        
        ---
        
        <gold>Prisoners (${prisoners.size}):</gold>
        
        <prisoners>
    """.trimIndent().mm(
        Placeholder.component("warden", wardenDisplay),
        Placeholder.component("guards", guardDisplay),
        Placeholder.component("prisoners", prisonerDisplay)
    ))
}

private fun displayPlayer(player: Player): Component {
    if (player.respawnTask != null)
        return "<dark_red>☠ <gray>${player.name}".mm

    return player.rankAndName()
        .appendSpace()
        .append(displayPing(player))
}

private fun displayPing(player: Player): Component {
    val ping = player.ping
    val color = if (ping > 400) "red"
        else if (ping > 200) "yellow"
        else "green"

    return "<gray>[<$color>${ping}ms</$color>]</gray>".mm
}