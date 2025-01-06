package me.honkling.prisonbutbad.profile

import me.honkling.prisonbutbad.config.configToml
import me.honkling.prisonbutbad.lib.mm
import me.honkling.prisonbutbad.wardenCooldown
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

private val profiles = mutableMapOf<Player, Profile>()
val Player.profile
    get() = profiles.computeIfAbsent(this) { Profile(this) }

fun Player.removeProfile()
    = profiles.remove(this)

class Profile(val player: Player) {
    val money by createKey<Double>(0.0)
    var role = Role.Prisoner
        set(value) {
            field = value

            when (value) {
                Role.Warden -> {
                    Bukkit.getServer().playSound(Sound.sound {
                        it.type(NamespacedKey.minecraft("block.end_portal.spawn"))
                    })
                    player.noDamageTicks = (configToml.warden.invulnerability * 20).toInt()
                    wardenCooldown = (configToml.warden.defaultCooldown * 20).toInt()
                }
                else -> {}
            }
        }

    fun reset() {
        if (role == Role.Warden) {
            Bukkit.broadcast("<green>The warden has resigned!".mm)
            wardenCooldown = (configToml.warden.resignCooldown * 20).toInt()
        }

        player.inventory.clear()
        role = Role.Prisoner
    }
}