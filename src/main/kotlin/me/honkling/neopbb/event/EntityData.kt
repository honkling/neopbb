package me.honkling.neopbb.event

import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import me.honkling.neopbb.event.packet.ClientboundPacket
import me.honkling.neopbb.packet.destroyEntityProfile
import me.honkling.neopbb.packet.getEntityProfile

@ClientboundPacket(PacketType.Play.Server.ENTITY_METADATA)
private fun onEntityMetadata(event: PacketSendEvent) {
    val packet = WrapperPlayServerEntityMetadata(event)
    val profile = getEntityProfile(packet.entityId)

    for (data in packet.entityMetadata)
        profile.process(data)
}

@ClientboundPacket(PacketType.Play.Server.DESTROY_ENTITIES)
private fun onDestroyEntities(event: PacketSendEvent) {
    val packet = WrapperPlayServerDestroyEntities(event)
    packet.entityIds.forEach(::destroyEntityProfile)
}