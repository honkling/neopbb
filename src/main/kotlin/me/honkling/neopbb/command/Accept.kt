@file:Command("accept")

package me.honkling.neopbb.command

import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.currentPrison
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.*
import org.bukkit.entity.Player

private fun accept(player: Player) {
    if (player.isRespawning)
        return player.sendMessage("<p>You cannot accept invitations while dead.".mm)

    if (player.inSolitary)
        return player.sendMessage("<p>You cannot accept invitations while in solitary.".mm)

    if (player.health <= 10)
        return player.sendMessage("<p>You cannot accept invitations while under half health.".mm)

    val invite = player.invite
        ?: return player.sendMessage("<p>You don't have any invitations.".mm)

    if (invite.role == Role.Warden && warden != null) {
        val pastWarden = warden!!

        if (pastWarden.health <= 10)
            return player.sendMessage("<p>Warden cannot be passed to you while the current warden is under half health.".mm)

        pastWarden.role = Role.Prisoner
        pastWarden.prepare(true, broadcast = false)
    }

    player.role = invite.role
    player.prepare(true, broadcast = true)
    invite.cancel()
    player.invite = null

    if (player.role.isAuthority && player.isInBlackMarket)
        player.teleport(currentPrison.blackMarketOut)
}