package me.honkling.neopbb.profile

import com.github.retrooper.packetevents.protocol.player.GameMode
import com.github.retrooper.packetevents.protocol.player.UserProfile
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate
import me.honkling.commonlib.lib.CaseType
import me.honkling.commonlib.lib.convertCase
import me.honkling.neopbb.config.configToml
import me.honkling.neopbb.feature.refreshTab
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.lib.onlinePlayers
import me.honkling.neopbb.packet.setGlowingFor
import me.honkling.neopbb.packetEvents
import me.honkling.neopbb.wardenCooldown
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import java.text.DecimalFormat

private val moneyFormat = DecimalFormat("#0.0")

private val profiles = mutableMapOf<Player, Profile>()
val Player.profile: Profile
    get() {
        return profiles.computeIfAbsent(this) { Profile(this) }
    }

fun Player.removeProfile()
    = profiles.remove(this)

class Profile(val player: Player) {
    var money by createKey<Double>(0.0)
    var pingNoises by createKey<Boolean>(true)
    var wardenSpaces by createKey<Boolean>(true)
    var showGlow by createKey<Boolean>(true) {
        for (aPlayer in onlinePlayers)
            aPlayer.profile.syncInTrouble(aPlayer)
    }

    var timeSinceLastRoleChange = 0
    var timeSinceLastWarden = -1

    var hasAttendedRollCall = false
    var isInBlackMarket = false
    var isInTeamChat = false
    var isInTrouble = false
        set(value) {
            println("Setting to $value")
            field = value

            for (player in onlinePlayers) {
                println("Syncing with $player")
                syncInTrouble(player)
            }
        }

    var invite: Invite? = null

    var role = Role.Prisoner
        set(value) {
            timeSinceLastRoleChange = 0
            field = value
            invite = null

            isInTrouble = false
            isInBlackMarket = false

            handleNameChange(value)

            when (value) {
                Role.Warden -> {
                    Bukkit.getServer().playSound(Sound.sound {
                        it.type(NamespacedKey.minecraft("block.end_portal.spawn"))
                    })
                    player.noDamageTicks = (configToml.warden.invulnerability * 20).toInt()
                    wardenCooldown = (configToml.warden.defaultCooldown * 20).toInt()
                    timeSinceLastWarden = 0
                }
                else -> {}
            }

            refreshTab()
        }

    init {
        handleNameChange(role)
    }

    fun displayMoney(): String {
        if (money.isInfinite()) {
            val isPositive = money >= 0
            return (if (isPositive) "" else "-") + "∞"
        }

        return moneyFormat.format(money)
    }

    fun syncInTrouble(player: Player) {
        println("Sync $isInTrouble")

        if (player.profile.showGlow)
            player.setGlowingFor(this.player, isInTrouble)

        refreshPlayerInfo(player)
    }

    fun refreshPlayerInfo(target: Player, sendPacket: Boolean = true): String {
        val color = "§" + when (role) {
            Role.Warden, Role.Escapee -> "c"
            Role.RiotGuard -> "8"
            Role.Guard -> "9"
            Role.Nurse -> "d"
            Role.Prisoner ->
                if (!showGlow && target.profile.isInTrouble) "4"
                else "6"
        }
        val formattedName = color + target.name

        if (sendPacket) {
            player.hidePlayer(instance, target)
            player.showPlayer(instance, target)
        }

        return formattedName
    }

    fun refreshPlayerInfo(sendPacket: Boolean = true) {
        for (aPlayer in onlinePlayers)
            refreshPlayerInfo(aPlayer, sendPacket)
    }

    fun reset() {
        if (role == Role.Warden) {
            Bukkit.broadcast("<green>The warden has resigned!".mm)
            wardenCooldown = (configToml.warden.resignCooldown * 20).toInt()
        }

        player.inventory.clear()
        role = Role.Prisoner
    }

    private fun handleNameChange(role: Role) {
        val name = role.name.convertCase(CaseType.Proper)
        player.displayName("<gray>[<role-color>$name</role-color>] <name-color>${player.name}".mm(
            Placeholder.styling("role-color", role.color),
            Placeholder.styling("name-color", role.nameColor)
        ))
    }
}