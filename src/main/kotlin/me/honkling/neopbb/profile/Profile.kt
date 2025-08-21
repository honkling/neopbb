package me.honkling.neopbb.profile

import me.honkling.neopbb.currentPrison
import me.honkling.neopbb.lib.getRandomCell
import me.honkling.neopbb.lib.mm
import me.honkling.neopbb.profile.key.NonPersistentKey
import me.honkling.neopbb.profile.key.createKey
import me.honkling.neopbb.refreshTab
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import kotlin.reflect.jvm.isAccessible

var Player.role by createKey(Role.Prisoner, persistent = false)
var Player.invite by createKey<Invite?>(false)
var Player.money by createKey(0.0f)
var Player.teamChat by createKey(fallbackValue = false)

var Player.attendedRollCall by createKey<Boolean>(false, persistent = false)
var Player.inCell by createKey<Boolean>(false, persistent = false)
var Player.isInBlackMarket by createKey<Boolean>(false, persistent = false)
var Player.solitaryTask by createKey<Int?>(persistent = false)
var Player.handcuffTask by createKey<Int?>(false)
var Player.respawnTask by createKey<Int?>(false)

val Player.inSolitary get() = solitaryTask != null
val Player.isRespawning get() = respawnTask != null

fun Player.prepare(reset: Boolean, broadcast: Boolean) {
    if (reset) {
        inventory.clear()
        health = getAttribute(Attribute.MAX_HEALTH)!!.value
        foodLevel = 20
        invite = null
    }

    if (role.isAuthority && broadcast) {
        val display = if (role == Role.Warden) "the warden" else "a ${role.name.lowercase()}"
        Bukkit.getServer().sendMessage("<p><s>$name</s> is now $display!".mm)
    }

    playSound(Sound.sound {
        it.type(Key.key("entity.zombie.break_wooden_door"))
    })

    role.team.addPlayer(this)
    role.prepare(this, reset)
    refreshTab()
}

fun Player.forceRespawn() {
    respawnTask?.let { Bukkit.getScheduler().cancelTask(it) }
    noDamageTicks = 20 * 5
    respawnTask = null

    gameMode = GameMode.SPECTATOR
    spectatorTarget = null
    gameMode = GameMode.ADVENTURE

    sendTitlePart(TitlePart.TITLE, Component.empty())
    sendTitlePart(TitlePart.SUBTITLE, Component.empty())
    prepare(true, broadcast = false)
    teleport(
        if (inSolitary) getRandomCell(currentPrison.solitaryCells)
        else currentPrison.respawn
    )
}

fun Player.cleanUp() {
    val nonPersistentFields = listOf(
        Player::role,
        Player::invite,
        Player::handcuffTask,
        Player::respawnTask,
        Player::attendedRollCall
    )

    for (field in nonPersistentFields) {
        field.isAccessible = true
        val key = field.getDelegate(this) as NonPersistentKey<*>
        key.cleanUp(this)
    }
}

fun Player.rankAndName(): Component {
    val prefix = role.prefix
    val name = name()

    return prefix.appendSpace()
        .append(name.color(if (role == Role.Warden) NamedTextColor.WHITE else NamedTextColor.GRAY))
}