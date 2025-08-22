package me.honkling.neopbb.discord

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.TextInputStyle
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.ModalSubmitInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.actionRow
import me.honkling.neopbb.config.configToml

internal suspend fun initializeReports() {
    val command = kord.createGuildChatInputCommand(channel.guildId, "reportembed", "Create report embed") {
        disableCommandInGuilds()
    }

    kord.on<ChatInputCommandInteractionCreateEvent> {
        if (interaction.invokedCommandId != command.id)
            return@on

        interaction.respondEphemeral { content = "oki doki" }
        interaction.channel.createMessage {
            content = "Click the button below to create a report."

            actionRow {
                interactionButton(ButtonStyle.Primary, "newreport") {
                    label = "Create Report"
                }
            }
        }
    }

    kord.on<ButtonInteractionCreateEvent> {
        if (interaction.componentId != "newreport")
            return@on

        interaction.modal("Create Report", "reportform") {
            actionRow {
                textInput(TextInputStyle.Short, "reportuser", "Who are you reporting?") {
                    required = true
                }
            }

            actionRow {
                textInput(TextInputStyle.Paragraph, "reportreason", "Why are you reporting them?") {
                    required = true
                }
            }
        }
    }

    kord.on<ModalSubmitInteractionCreateEvent> {
        if (interaction.modalId != "reportform")
            return@on

        val user = interaction.user
        val channel = interaction.getChannel() as TextChannel
        val thread = channel.startPrivateThread("${user.username}'s report")

        val username = interaction.textInputs["reportuser"]!!.value!!
        val reason = interaction.textInputs["reportreason"]!!.value!!
        val embed = EmbedBuilder().apply {
            title = "Report Details"
            description = """
                **Reported Player:** `${username.replace('`', '\'')}`
                **Reason:**
                ```
                ${reason.replace('`', '\'')}
                ```
            """.trimIndent()
        }

        thread.createMessage {
            content = "Welcome, ${user.mention}. A ${staffRole.mention} will get to you shortly."
            embeds = mutableListOf(embed)
            allowedMentions = AllowedMentionsBuilder().apply {
                users += user.id
                roles += configToml.discord.staffId
            }
        }

        interaction.respondEphemeral {
            content = "The report has been filed in ${thread.mention}. Thank you!"
        }
    }
}