@file:Listener

package me.honkling.neopbb.event

import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.command.teamchat
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.profile
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.PatternReplacementResult
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player

private object Renderer : ChatRenderer {
    override fun render(source: Player, sourceDisplayName: Component, message: Component, viewer: Audience): Component {
        val uuid = viewer.get(Identity.UUID)
        val profile = source.profile
        val role = profile.role

        @Suppress("NAME_SHADOWING")
        var message = message

        if (uuid.isPresent) run {
            val player = Bukkit.getPlayer(uuid.get())
                ?: return@run

            message = message.replaceText { builder ->
                builder.match("(?i)\\b${player.name}\\b")
                    .condition { _, _, replaced ->
                        if (replaced == 0 && player.profile.pingNoises)
                            player.playSound(Sound.sound {
                                it.type(Key.key("block.note_block.pling"))
                                    .pitch(2f)
                            })

                        PatternReplacementResult.REPLACE
                    }
                    .replacement(Component.text("@${player.name}")
                        .color(NamedTextColor.GREEN))
            }
        }

        val delimiter = Component.text(": ")
            .color(if (role == Role.Warden) NamedTextColor.RED else NamedTextColor.GRAY)

        var completeMessage = sourceDisplayName.append(delimiter.append(message))

        if (profile.wardenSpaces && role == Role.Warden) {
            completeMessage = Component.newline()
                .append(completeMessage)
                .append(Component.newline())

            viewer.playSound(Sound.sound {
                it.type(Key.key("block.note_block.bit"))
            })
        }

        return completeMessage
    }
}

private fun onChat(event: AsyncChatEvent) {
    val player = event.player

    if (player.profile.isInTeamChat) {
        val plainMessage = PlainTextComponentSerializer.plainText().serialize(event.originalMessage())
        teamchat(player, plainMessage)
        event.isCancelled = true
        return
    }

    event.renderer(Renderer)
}