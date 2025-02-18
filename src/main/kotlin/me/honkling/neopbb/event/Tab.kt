package me.honkling.neopbb.event

import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate
import me.honkling.neopbb.event.packet.ClientboundPacket
import me.honkling.neopbb.profile.profile
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@ClientboundPacket(PacketType.Play.Server.PLAYER_INFO)
private fun onPlayerInfoAdd(event: PacketSendEvent) {
    val packet = WrapperPlayServerPlayerInfo(event)

    for (entry in packet.playerDataList) {
        val profile = entry.userProfile
//        profile.name = "§c${profile.name}"
    }

//    event.markForReEncode(true)
}

@ClientboundPacket(PacketType.Play.Server.PLAYER_INFO_UPDATE)
private fun onPlayerInfoUpdate(event: PacketSendEvent) {
    val packet = WrapperPlayServerPlayerInfoUpdate(event)
    val player = event.getPlayer<Player>()
    val showGlow = player.profile.showGlow

    for (entry in packet.entries) {
        val gameProfile = entry.gameProfile
        val entryPlayer = Bukkit.getPlayer(gameProfile.uuid)
        val profile = entryPlayer?.profile
            ?: continue

        println("${player.name} ($showGlow) -> ${entryPlayer.name} (${profile?.isInTrouble})")

        entry.gameProfile.name = player.profile.refreshPlayerInfo(entryPlayer, false)
        entry.isListed = false
    }

    event.markForReEncode(true)
}