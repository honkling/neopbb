package me.honkling.neopbb

import net.kyori.adventure.text.Component
import org.bukkit.entity.EntityType
import org.bukkit.entity.Villager

lateinit var bertrude: Villager; private set

fun spawnBertrude() {
    val location = currentPrison.bertrude
    bertrude = location.world.spawnEntity(location, EntityType.VILLAGER) as Villager
    bertrude.customName(Component.text("bertrude (real settings)"))
    bertrude.setAI(false)
    bertrude.setGravity(false)
    bertrude.isInvulnerable = true
    bertrude.isPersistent = false
}