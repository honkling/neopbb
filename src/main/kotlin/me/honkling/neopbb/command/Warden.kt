@file:Command("warden")

package me.honkling.neopbb.command

import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.time.ExperimentalTime

private fun warden(player: Player) {
    if (warden != null)
        return player.sendMessage("<p>There is already a warden.".mm)

    if (wardenCooldown > 0)
        return player.sendMessage("<p>You must wait before you can become the warden.".mm)

    if (player.isRespawning)
        return player.sendMessage("<p>You must wait to respawn.".mm)

    if (lastWarden == player)
        return player.sendMessage("<p>You cannot be the warden a second time.".mm)

    lastWarden = player
    player.role = Role.Warden
    player.prepare(true, broadcast = true)
}

private fun hire(player: Player, target: Player, role: Role) {
    if (warden != player)
        return player.sendMessage("<p>You aren't the warden.".mm)

    if (target.role.isAuthority)
        return player.sendMessage("<p><s>${target.name}</s> is already a guard.".mm)

    if (target.invite != null)
        return player.sendMessage("<p><s>${target.name}</s> already has an ongoing invitation.".mm)

    if (role == Role.Swat && !swatUnlocked)
        return player.sendMessage("<p>You don't have SWAT Guards unlocked!".mm)

    player.sendMessage("<p><s>${target.name}</s> has been sent an invitation.".mm)
    target.sendMessage("\n<p>The warden wants you to be a ${role.name.lowercase()}!\n<p><s><u><click:run_command:/accept>Accept</s>\n".mm)
    target.invite = Invite(target, role).schedule()
}

private fun `hire$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return when (node.name) {
        "target" -> Bukkit.getOnlinePlayers()
            .filter { !it.role.isAuthority }
            .map { it.name }
            .filter { it.contains(input, true) }
        "role" -> Role.entries
            .filter { it.isAuthority && it != Role.Warden && (it != Role.Swat || swatUnlocked) }
            .map { it.name }
            .filter { it.contains(input, true) }
        else -> emptyList()
    }
}

private fun fire(player: Player, target: Player) {
    if (warden != player)
        return player.sendMessage("<p>You aren't the warden.".mm)

    if (!target.role.isAuthority || target.role == Role.Warden)
        return player.sendMessage("<p><s>${target.name}</s> isn't a guard.".mm)

    Bukkit.getServer().sendMessage("<p><s>${target.name}</s> has been fired!".mm)
    target.role = Role.Prisoner
    target.prepare(true, broadcast = true)
}

private fun `fire$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return Bukkit.getOnlinePlayers()
        .filter { it.role.isAuthority && it.role != Role.Warden }
        .map { it.name }
        .filter { it.contains(input, true) }
}

@OptIn(ExperimentalTime::class)
private fun solitary(sender: Player, player: Player) {
    if (warden != sender)
        return sender.sendMessage("<p>You aren't the warden.".mm)

    if (player.inSolitary)
        return sender.sendMessage("<p>They are already in solitary.".mm)

    if (player.role.isAuthority)
        return sender.sendMessage("<p>You can't send a guard to solitary.".mm)

    if (!player.isRespawning)
        return sender.sendMessage("<p>They must be dead to be put in solitary.".mm)

    var player = player
    player.role = Role.Solitary
    player.solitaryTask = Bukkit.getScheduler().scheduleSyncDelayedTask(instance, {
        Bukkit.getPlayer(player.uniqueId)?.let { player = it } // Refresh player instance in case they relogged
        player.solitaryTask = null
        player.role = Role.Prisoner
        player.prepare(true, broadcast = true)

        if (!player.isOnline)
            player.cleanUp()
    }, 20L * 120)
    player.forceRespawn()
}

private fun release(sender: Player, player: Player) {
    if (warden != sender)
        return sender.sendMessage("<p>You aren't the warden.".mm)

    if (!player.inSolitary)
        return sender.sendMessage("<p>They aren't in solitary.".mm)

    player.role = Role.Prisoner
    player.solitaryTask?.let { Bukkit.getScheduler().cancelTask(it) }
    player.solitaryTask = null

    if (player.isRespawning)
        player.forceRespawn()
    else player.prepare(true, broadcast = true)
}

private fun help(sender: CommandSender) {
    sender.sendMessage("""
        <p>Here are the commands you can run:
        <p><s>/warden</s> - Become the warden if there is none.
        <p><s>/warden hire (player) <nurse/guard/swat></s> - Hire a guard.
        <p><s>/warden fire (player)</s> - Fire a guard.
        <p><s>/warden solitary (player)</s> - Put a player into solitary.
        <p><s>/warden release (player)</s> - Release a player from solitary.
    """.trimIndent().mm)
}