package me.honkling.neopbb.event.packet

import com.github.retrooper.packetevents.protocol.packettype.PacketType

@Target(AnnotationTarget.FUNCTION)
annotation class ServerboundPacket(val type: PacketType.Play.Client)

@Target(AnnotationTarget.FUNCTION)
annotation class ClientboundPacket(val type: PacketType.Play.Server)
