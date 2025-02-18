@file:Listener

package me.honkling.neopbb.event

import me.honkling.commando.spigot.event.Listener
import me.honkling.neopbb.gui.CraftGUI
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.prison
import me.honkling.neopbb.profile.Role
import me.honkling.neopbb.profile.profile
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent

private fun onUseCraftingTable(event: PlayerInteractEvent) {
    if (event.clickedBlock?.type != Material.CRAFTING_TABLE)
        return

    event.isCancelled = true
    CraftGUI.SelectRecipe().open(event.player)
}

private fun onUseBlackMarket(event: PlayerInteractEvent) {
    if (event.clickedBlock?.type != Material.CAULDRON)
        return

    val player = event.player
    val profile = player.profile
    val inBlackMarket = profile.isInBlackMarket
    event.isCancelled = true

    if (profile.role.isAuthority && profile.role != Role.Warden)
        return player.sendMessage("<failure>You can't be in there!".mm)

    profile.isInBlackMarket = !inBlackMarket
    player.teleport(if (inBlackMarket)
        prison.blackMarketExit
    else prison.blackMarketEnter)
}