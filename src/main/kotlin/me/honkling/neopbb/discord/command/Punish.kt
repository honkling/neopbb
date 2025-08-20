package me.honkling.neopbb.discord.command

import dev.kord.common.entity.Choice
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.optional.Optional
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.suggest
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.GuildAutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.interaction.string
import kotlinx.coroutines.launch
import me.honkling.neopbb.config.configToml
import me.honkling.neopbb.discord.DiscordSender
import me.honkling.neopbb.discord.channel
import me.honkling.neopbb.discord.kord
import me.honkling.neopbb.instance
import me.honkling.neopbb.scope
import me.honkling.ruby.config.punishmentsToml
import me.honkling.ruby.lib.hasPermission
import me.honkling.ruby.punishment.calculateDuration
import me.honkling.ruby.punishment.issuePunishment
import org.bukkit.Bukkit

internal suspend fun registerPunish() {
    val command = kord.createGuildChatInputCommand(channel.guildId, "punish", "Punish a player on neopbb.") {
        defaultMemberPermissions = Permissions(Permission.ModerateMembers)

        string("username", "The player's username.") {
            required = true
        }

        string("reason", "The punishment reason.") {
            required = true
            autocomplete = true
        }

        string("notes", "The punishment's notes") {
            required = false
        }
    }

    kord.on<GuildAutoCompleteInteractionCreateEvent> {
        if (interaction.command.rootId != command.id)
            return@on

        val value = interaction.focusedOption.value
        interaction.suggest(punishmentsToml.reasons.values.filter { it.name.contains(value, true) }
            .map { Choice.StringChoice(
                it.name,
                Optional(),
                it.name
            ) })
    }

    kord.on<GuildChatInputCommandInteractionCreateEvent> {
        if (interaction.invokedCommandId != command.id)
            return@on

        val member = interaction.user.asMember(interaction.guildId)

        if (configToml.discord.staffId !in member.roleIds) {
            interaction.respondEphemeral { content = "Only NeoPBB staff can execute this command." }
            return@on
        }

        val username = interaction.command.strings["username"]!!
        val reasonName = interaction.command.strings["reason"]!!
        val notes = interaction.command.strings["notes"]
        val player = Bukkit.getOfflinePlayer(username)
        val reason = punishmentsToml.reasons.entries.find { it.value.name.equals(reasonName, true) }?.value

        if (reason == null) {
            interaction.respondEphemeral { content = "Couldn't find that reason." }
            return@on
        }

        if (player.hasPermission("ruby.punish")) {
            interaction.respondEphemeral { content = "You can't punish moderators." }
            return@on
        }

        Bukkit.getScheduler().runTask(instance, Runnable {
            val result = issuePunishment(
                DiscordSender(interaction.user),
                player,
                reason,
                calculateDuration(player, reason),
                notes ?: ""
            )

            scope.launch {
                interaction.respondPublic {
                    content = if (result.isSuccess) {
                        val duration = result.getOrThrow().duration
                        val displayDuration = if (reason.type.hasDuration) " (${
                            duration.toString().replace("Infinity", "permanent")
                        })" else ""
                        "Punished ${player.name} for ${reason.name}$displayDuration"
                    } else result.exceptionOrNull()?.message ?: "An error occurred."
                }
            }
        })
    }
}