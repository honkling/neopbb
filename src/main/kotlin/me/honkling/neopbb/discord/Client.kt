package me.honkling.neopbb.discord

import dev.kord.core.Kord
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.TextChannel
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import me.honkling.neopbb.config.configToml
import me.honkling.neopbb.discord.command.registerPunish
import me.honkling.neopbb.instance
import org.bukkit.Bukkit

internal lateinit var kord: Kord; private set
internal lateinit var channel: TextChannel; private set
internal lateinit var reports: TextChannel; private set
internal lateinit var staffLogs: TextChannel; private set
internal lateinit var staffRole: Role; private set

suspend fun initializeKord() {
    kord = Kord(configToml.discord.token)

    channel = kord.getChannelOf<TextChannel>(configToml.discord.channelId)
        ?: return instance.logger.severe("Kord couldn't find the chat channel. Is the channel ID set?")

    reports = kord.getChannelOf<TextChannel>(configToml.discord.reportsId)
        ?: return instance.logger.severe("Kord couldn't find the reports channel. Is the channel ID set?")

    staffLogs = kord.getChannelOf<TextChannel>(configToml.discord.staffLogsId)
        ?: return instance.logger.severe("Kord couldn't find the staff logs channel. Is the channel ID set?")

    staffRole = channel.guild.getRoleOrNull(configToml.discord.staffId)
        ?: return instance.logger.severe("Kord couldn't find the staff role. Is the role ID set?")

    initializeEvents()
    initializeReports()
    registerPunish()

    kord.login {
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent
    }

    if (!Bukkit.getServer().isStopping)
        initializeKord()
}