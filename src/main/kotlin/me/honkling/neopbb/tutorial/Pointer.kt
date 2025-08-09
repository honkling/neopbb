package me.honkling.neopbb.tutorial

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.util.Vector3f
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.honkling.neopbb.packetEvents
import me.honkling.neopbb.profile.key.createKey
import me.tofaa.entitylib.meta.display.TextDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player

private var Player.pointers by createKey<MutableList<Pointer>>(mutableListOf(), persistent = false)

data class Pointer(val player: Player, val location: Location) : WrapperEntity(EntityTypes.TEXT_DISPLAY) {
    val entityMeta: TextDisplayMeta = getEntityMeta(TextDisplayMeta::class.java)

    init {
        val user = packetEvents.playerManager.getUser(player)
        addViewer(user)
        spawn(SpigotConversionUtil.fromBukkitLocation(player.location))
        user.sendPacket(WrapperPlayServerSetPassengers(user.entityId, intArrayOf(entityId)))
        entityMeta.scale = Vector3f(2f, 2f, 2f)
        entityMeta.translation = Vector3f(1f, -player.eyeHeight.toFloat() / 2, 0f)
        entityMeta.text = Component.text("^")
        entityMeta.isSeeThrough = true
        entityMeta.backgroundColor = 0x00000000
    }
}

fun Player.setPointers(vararg locations: Location) {
    pointers.forEach { it.remove() }
    pointers = locations.map { Pointer(this, it) }.toMutableList()
}