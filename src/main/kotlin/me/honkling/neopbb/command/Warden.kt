@file:Command("warden")

package me.honkling.neopbb.command

import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.getCooldown
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

    val elapsed = System.currentTimeMillis() - lastSolitary
    val cooldown = 1_000 * 60 * 2.5

    if (elapsed < cooldown)
        return sender.sendMessage("<p>This is still on cooldown. <s>[${getCooldown(cooldown - elapsed)}]</s>".mm)

    var player = player
    player.role = Role.Solitary
    player.solitaryTask = Bukkit.getScheduler().scheduleSyncDelayedTask(instance, {
        Bukkit.getPlayer(player.uniqueId)?.let { player = it } // Refresh player instance in case they relogged

        if(player.role == Role.Solitary) {
            player.role = Role.Prisoner
            player.prepare(true, broadcast = true)
        }

        player.solitaryTask = null

        if (!player.isOnline)
            player.cleanUp()
    }, 20L * 120)
    player.forceRespawn()
    lastSolitary = System.currentTimeMillis()
}

private fun `solitary$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return Bukkit.getOnlinePlayers()
        .filter { !it.role.isAuthority && !it.inSolitary && it.isRespawning }
        .map { it.name }
        .filter { it.contains(input, true) }
}

private fun release(sender: Player, player: Player) {
    if (warden != sender)
        return sender.sendMessage("<p>You aren't the warden.".mm)

    if (!player.inSolitary)
        return sender.sendMessage("<p>They aren't in solitary.".mm)

    player.role = Role.Prisoner
    player.solitaryTask?.let { Bukkit.getScheduler().cancelTask(it) }

    if (player.isRespawning)
        player.forceRespawn()
    else player.prepare(true, broadcast = true)
}

private fun `release$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return Bukkit.getOnlinePlayers()
        .filter { it.inSolitary }
        .map { it.name }
        .filter { it.contains(input, true) }
}

private fun pass(sender: Player, player: Player){
    if(warden != sender)
        return sender.sendMessage("<p>You aren't the warden.".mm)

    if(player.isRespawning)
        return sender.sendMessage("<p>The player named <s>${player.name}</s> is respawning.".mm)

    if(player.inSolitary)
        return sender.sendMessage("<p>The player named <s>${player.name}</s> is in solitary.".mm)

    if (player.invite != null)
        return sender.sendMessage("<p><s>${player.name}</s> already has an ongoing invitation.".mm)

    if(sender.health <= 10)
        return sender.sendMessage("<p>You must be above 10 hearts to pass warden.".mm)

    if(player.health <= 10)
        return sender.sendMessage("<p>You cannot pass warden to the player named ${player.name} as they are below 10 hearts.".mm)

    if(player == sender)
        return sender.sendMessage("<p>You can't swap warden to yourself.".mm)

    sender.sendMessage("<p><s>${player.name}</s> has been sent an invitation.".mm)
    player.sendMessage("\n<p>The current warden wants you to become the warden!\n<p><s><u><click:run_command:/accept>Accept</s>\n".mm)
    player.invite = Invite(player, Role.Warden).schedule()
}

private fun `pass$complete`(sender: CommandSender, node: ParameterNode<Command>, input: String): List<String> {
    return Bukkit.getOnlinePlayers()
        .filter { !it.isRespawning && !it.inSolitary && it.invite == null }
        .map { it.name }
        .filter { it.contains(input, true) }
}

private fun help(sender: CommandSender) {
    sender.sendMessage("""
        <p>Here are the commands you can run:
        <p><s>/warden</s> - Become the warden if there is none.
        <p><s>/warden hire (player) <nurse/guard/swat></s> - Hire a guard.
        <p><s>/warden fire (player)</s> - Fire a guard.
        <p><s>/warden solitary (player)</s> - Put a player into solitary.
        <p><s>/warden release (player)</s> - Release a player from solitary.
        <p><s>/warden pass (player)</s> - Swaps the warden to someone else.
    """.trimIndent().mm)
}