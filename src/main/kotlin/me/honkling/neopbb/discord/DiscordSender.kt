package me.honkling.neopbb.discord

import dev.kord.core.entity.User
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.permissions.PermissionAttachmentInfo
import org.bukkit.plugin.Plugin
import java.util.UUID
import javax.naming.OperationNotSupportedException

class DiscordSender(val user: User) : CommandSender {
    override fun sendMessage(message: String) {}
    override fun sendMessage(vararg messages: String) {}
    @Deprecated("Deprecated in Java")
    override fun sendMessage(sender: UUID?, message: String) {}
    @Deprecated("Deprecated in Java")
    override fun sendMessage(sender: UUID?, vararg messages: String) {}
    override fun getServer(): Server {
        return Bukkit.getServer()
    }

    override fun getName(): String {
        return user.username
    }

    override fun spigot(): CommandSender.Spigot {
        return CommandSender.Spigot()
    }

    override fun name(): Component {
        return Component.text(name)
    }

    override fun isPermissionSet(name: String): Boolean {
        return false
    }

    override fun isPermissionSet(perm: Permission): Boolean {
        return false
    }

    override fun hasPermission(name: String): Boolean {
        return false
    }

    override fun hasPermission(perm: Permission): Boolean {
        return false
    }

    override fun addAttachment(
        plugin: Plugin,
        name: String,
        value: Boolean
    ): PermissionAttachment {
        throw OperationNotSupportedException()
    }

    override fun addAttachment(plugin: Plugin): PermissionAttachment {
        throw OperationNotSupportedException()
    }

    override fun addAttachment(
        plugin: Plugin,
        name: String,
        value: Boolean,
        ticks: Int
    ): PermissionAttachment? = null

    override fun addAttachment(
        plugin: Plugin,
        ticks: Int
    ): PermissionAttachment? = null

    override fun removeAttachment(attachment: PermissionAttachment) {}

    override fun recalculatePermissions() {}

    override fun getEffectivePermissions(): Set<PermissionAttachmentInfo?> {
        return emptySet()
    }

    override fun isOp(): Boolean {
        return false
    }

    override fun setOp(value: Boolean) {}

}