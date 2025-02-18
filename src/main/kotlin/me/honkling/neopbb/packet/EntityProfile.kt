package me.honkling.neopbb.packet

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.player.User
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import org.bukkit.entity.Player

private val profiles = mutableMapOf<Int, EntityProfile>()

class EntityProfile(val id: Int) {
    val data = mutableMapOf<Int, EntityData>()

    fun process(data: EntityData) {
        this.data[data.index] = data
    }

    fun update(user: User) {
        user.sendPacket(WrapperPlayServerEntityMetadata(
            id,
            data.values.toMutableList()
        ))
    }
}

fun getEntityProfile(id: Int)
    = profiles.computeIfAbsent(id) { EntityProfile(id) }

fun destroyEntityProfile(id: Int)
    = profiles.remove(id)