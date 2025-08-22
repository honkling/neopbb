@file:Listener

package me.honkling.neopbb.event

import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.EmbedBuilder
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.launch
import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.config.filterToml
import me.honkling.neopbb.discord.staffLogs
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.scope
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit

internal fun runFilter(event: AsyncChatEvent) {
    val player = event.player
    val input = PlainTextComponentSerializer.plainText().serialize(event.message())
    val staff = Audience.audience(Bukkit.getOnlinePlayers().filter { it.hasPermission("neopbb.filter") })

    for (rule in filterToml.rules) {
        if (!rule.test(input))
            continue

        event.isCancelled = true

        Bukkit.getScheduler().runTask(instance, Runnable {
            rule.action.act(event, rule)
            staff.sendMessage("<p><s>${player.name}</s> triggered chat filter rule <s>${rule.name}</s>:\n<p>$input".mm)

            val embed = EmbedBuilder().apply {
                title = "Filter Violation"
                description = """
                    `${player.name}` violated filter rule ${rule.name}:
                    `${input.replace('`', '\'')}`
                """.trimIndent()
            }

            scope.launch {
                staffLogs.createMessage { embeds = mutableListOf(embed) }
            }
        })
    }
}