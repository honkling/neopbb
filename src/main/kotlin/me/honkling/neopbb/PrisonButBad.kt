package me.honkling.neopbb

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import com.github.retrooper.packetevents.event.PacketListenerPriority
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.honkling.commando.spigot.SpigotCommando
import me.honkling.commonlib.CommonLib
import me.honkling.neopbb.command.type.ItemType
import me.honkling.neopbb.event.packet.PacketInteraction
import me.honkling.neopbb.feature.FeatureInteraction
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

val instance = JavaPlugin.getPlugin(PrisonButBad::class.java)
val logger = instance.logger

lateinit var commando: SpigotCommando; private set
lateinit var packetEvents: PacketEventsAPI<Plugin>; private set

class PrisonButBad : JavaPlugin() {
    override fun onLoad() {
        packetEvents = SpigotPacketEventsBuilder.build(this)
        PacketEvents.setAPI(packetEvents)
        packetEvents.load()
    }

    override fun onEnable() {
        packetEvents.init()
        CommonLib(this)
        commando = SpigotCommando(this)
        val packetInteraction = PacketInteraction()
        commando.interactionRegistry.register(packetInteraction)
        commando.interactionRegistry.register(FeatureInteraction())
        commando.typeRegistry.register(ItemType(), ItemStack::class)
        commando.register("me.honkling.neopbb", "command", "event", "feature")

        packetEvents.eventManager.registerListener(packetInteraction, PacketListenerPriority.NORMAL)
    }

    override fun onDisable() {
        packetEvents.terminate()
    }
}
