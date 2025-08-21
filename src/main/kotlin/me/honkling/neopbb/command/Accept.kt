@file:Command("accept")

package me.honkling.neopbb.command

import me.honkling.commando.spigot.command.Command
import me.honkling.neopbb.currentPrison
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.*
import org.bukkit.entity.Player

private fun accept(player: Player) {
    if(player.isRespawning)
        return player.sendMessage("<p>You can't accept invitations during respawning.".mm)

    if(player.inSolitary)
        return player.sendMessage("<p>You can't accept invitations while in solitary".mm)

    if(player.health <= 10)
        return player.sendMessage("<p>You can't accept invitations while under 10 hearts.".mm)

    val invite = player.invite
        ?: return player.sendMessage("<p>You don't have any invitations.".mm)

    val isWardenPass = invite.role == Role.Warden && warden != null;

    if(isWardenPass) {
        val pastWarden = warden

        pastWarden?.health?.let {
            if(it <= 10)
                return player.sendMessage("<p>You can't accept invitation for passing warden until current warden is above 5 hearts.".mm)
        }

        pastWarden?.role = Role.Prisoner
        pastWarden?.prepare(true, broadcast = false)
    }

    player.role = invite.role
    player.prepare(true, broadcast = true)
    invite.cancel()
    player.invite = null

    if (player.role.isAuthority && player.isInBlackMarket)
        player.teleport(currentPrison.blackMarketOut)
}