@file:Feature("Tab")
@file:Listener

package me.honkling.neopbb.feature

import me.honkling.commando.spigot.event.Listener
import me.honkling.commonlib.scheduler
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.lib.onlinePlayers
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.profile
import me.honkling.neopbb.wardens
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.math.round

private fun schedule(): Int {
    return scheduler.scheduleSyncRepeatingTask(instance, ::execute, 0L, 100L)
}

private fun execute() {
    refreshTab()
}

fun refreshTab() {
    val players = onlinePlayers
    val wardens = wardens

    val displayWarden =
        if (wardens.isEmpty()) Component.text("No warden!")
        else {
            var backingComponent = Component.empty()

            for ((index, warden) in wardens.withIndex()) {
                backingComponent = Component.empty()
                    .append(warden.displayName())
                    .append(Component.space())
                    .append(displayPing(warden))

                var hpTime = Component.text("(${round(warden.health).toInt()} HP")
                    .color(NamedTextColor.RED)

                if (warden.noDamageTicks > 0) {
                    val displayDamage = warden.noDamageTicks / 20
                    hpTime = hpTime.append(Component.space())
                        .append(Component.text("[${displayDamage}s of No-Damage]")
                            .color(NamedTextColor.AQUA))
                }

                val timer = warden.profile.timeSinceLastRoleChange / (20 * 60)
                hpTime = hpTime.append(Component.text(") for ${timer}m"))

                backingComponent =
                    if (wardens.size == 1)
                        backingComponent.append(Component.newline())
                            .append(hpTime)
                    else backingComponent.append(Component.space())
                        .append(hpTime)

                if (index + 1 < wardens.size)
                    backingComponent = backingComponent.append(Component.newline())
            }

            backingComponent
        }

    var displayGuards = Component.empty()
    var guardCount = 0

    var displayPrisoners = Component.empty()
    var prisonerCount = 0

    for (player in players) {
        val profile = player.profile

        if (profile.role == Role.Warden)
            continue

        val component =
            if (player.isDead) Component.text("☠")
                .color(NamedTextColor.DARK_RED)
                .append(Component.space())
                .append(player.name()
                    .color(NamedTextColor.RED))
                .append(Component.newline())
            else Component.empty()
                .append(player.displayName())
                .append(Component.space())
                .append(displayPing(player))
                .append(Component.newline())

        if (profile.role.isAuthority) {
            displayGuards = displayGuards.append(component)
            guardCount++
        } else {
            displayPrisoners = displayPrisoners.append(component)
            prisonerCount++
        }
    }

    val tab = """
        <gray>---
        <yellow>NeoPBB</yellow>
        <green>Players: ${onlinePlayers.size}</green>
        <red><warden></red>
        ---
        
        <aqua>Guards (${guardCount}):</aqua>
        
        <guards>
        
        ---
        
        <gold>Prisoners (${prisonerCount}):
        
        <prisoners>
    """.trimIndent().mm(
        Placeholder.component("warden", displayWarden),
        Placeholder.component("guards", displayGuards),
        Placeholder.component("prisoners", displayPrisoners)
    )

    for (player in players)
        player.sendPlayerListHeader(tab)
}

private fun displayPing(player: Player): Component {
    val ping = player.ping

    return Component.text("[")
        .color(NamedTextColor.GRAY)
        .append(Component
            .text("${ping}ms")
            .color(when (ping) {
                in 0..<200 -> NamedTextColor.GREEN
                in 200..<400 -> NamedTextColor.YELLOW
                in 400..Int.MAX_VALUE -> NamedTextColor.RED
                else -> NamedTextColor.GREEN
            }))
        .append(Component.text("]"))
}

private fun onPlayerJoin(event: PlayerJoinEvent) {
    refreshTab()
}

private fun onPlayerQuit(event: PlayerQuitEvent) {
    refreshTab()
}