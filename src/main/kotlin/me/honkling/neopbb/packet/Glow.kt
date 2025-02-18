package me.honkling.neopbb.packet

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import me.honkling.neopbb.packetEvents
import org.bukkit.entity.Player
import kotlin.experimental.and
import kotlin.experimental.or

private const val glowingFlag = 0x40

fun Player.setGlowingFor(target: Player, glowing: Boolean) {
    val user = packetEvents.playerManager.getUser(this)
    val profile = getEntityProfile(target.entityId)
    var state = when (val value = profile.data[0]?.value) {
        is Int -> value.toByte()
        is Byte -> value
        else -> 0
    }

    state = if (glowing) state.or(glowingFlag.toByte())
        else state.and(glowingFlag.toByte())

    profile.process(EntityData(0, EntityDataTypes.BYTE, state))
    profile.update(user)
}