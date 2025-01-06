package me.honkling.prisonbutbad

import me.honkling.commando.spigot.SpigotCommando
import me.honkling.commonlib.CommonLib
import me.honkling.prisonbutbad.command.type.ItemType
import me.honkling.prisonbutbad.feature.FeatureInteraction
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

val instance = JavaPlugin.getPlugin(PrisonButBad::class.java)
val logger = instance.logger

class PrisonButBad : JavaPlugin() {
    override fun onEnable() {
        CommonLib(this)
        val commands = SpigotCommando(this)
        commands.interactionRegistry.register(FeatureInteraction())
        commands.typeRegistry.register(ItemType(), ItemStack::class)
        commands.register("me.honkling.prisonbutbad", "command", "event", "feature")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
