@file:Command("warden")

package me.honkling.neopbb.command

import me.honkling.commando.spigot.command.Command
import me.honkling.commonlib.lib.scheduleTemporarily
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.Invite
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.profile
import me.honkling.neopbb.wardens
import org.bukkit.Bukkit
import org.bukkit.entity.Player

private const val passTimeRequirement = 180

private fun warden(sender: Player) {
    if (wardens.isNotEmpty())
        return sender.sendMessage("<failure>There is already a warden.".mm)

    for (player in Bukkit.getOnlinePlayers())
        if (player.profile.role.isAuthority)
            player.profile.reset()

    sender.profile.role = Role.Warden
}

private fun pass(sender: Player, target: Player) {
    val senderProfile = sender.profile

    if (senderProfile.role != Role.Warden)
        return sender.sendMessage("<failure>You aren't the warden.".mm)

    if (sender == target)
        return sender.sendMessage("<failure>You're already the warden'.".mm)

    val targetProfile = target.profile
    val timeSinceLastWarden = targetProfile.timeSinceLastWarden

    if (targetProfile.role == Role.Warden)
        return sender.sendMessage("<failure>They are already a warden.".mm)

    if (timeSinceLastWarden != -1 && timeSinceLastWarden < passTimeRequirement * 20) {
        val minutes = passTimeRequirement / 60.0
        return sender.sendMessage("<failure>That person was a warden in the last $minutes minutes.".mm)
    }

    val seconds = 10
    sender.sendMessage("<info>Please wait <info2>$seconds seconds</info2> for the keys to be passed.".mm)

    scheduleTemporarily("warden-pass-${sender.uniqueId}") {
        val initialPosition = sender.location

        task(1) {
            val position = sender.location

            if (position.x != initialPosition.x ||
                position.y != initialPosition.y ||
                position.z != initialPosition.z
            ) {
                sender.sendMessage("<failure>You moved! Cancelled.".mm)
                resolve()
            }
        }

        task(seconds * 20) {
            targetProfile.invite = Invite(sender, Role.Warden)
            target.sendMessage("<info><info2>${sender.name}</info2> wants you to be the warden! <b><info2><click:run_command:/accept>Accept".mm)
            sender.sendMessage("<success>An invite has been sent out.".mm)
            resolve()
        }

        task(seconds * 20 + 30 * 20) {
            if (targetProfile.invite != null) {
                targetProfile.invite = null
                sender.sendMessage("<failure>The invitation has expired.".mm)
                target.sendMessage("<failure>Your invitation has expired.".mm)
                resolve()
            }
        }
    }
}