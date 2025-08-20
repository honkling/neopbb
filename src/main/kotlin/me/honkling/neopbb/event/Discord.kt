@file:Listener

package me.honkling.neopbb.event

import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.launch
import me.honkling.commando.spigot.event.Listener
import me.honkling.commando.spigot.event.Priority
import me.honkling.neopbb.discord.channel
import me.honkling.neopbb.profile.rankAndName
import me.honkling.neopbb.scope
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

@Priority(EventPriority.HIGHEST)
private fun onChat(event: AsyncChatEvent) {
    if (event.isCancelled)
        return

    val serializer = PlainTextComponentSerializer.plainText()
    val username = replaceMessage(serializer.serialize(event.player.rankAndName()))
    val message = replaceMessage(serializer.serialize(event.message()))

    scope.launch {
        channel.createMessage("**$username**: $message")
    }
}

private fun onJoin(event: PlayerJoinEvent) {
    val username = replaceMessage(event.player.name)

    scope.launch {
        channel.createMessage("**$username** is now in prison.")
    }
}

private fun onQuit(event: PlayerQuitEvent) {
    val username = replaceMessage(event.player.name)

    scope.launch {
        channel.createMessage("**$username** ran off...")
    }
}

@Priority(EventPriority.HIGHEST)
private fun onDeath(event: PlayerDeathEvent) {
    if (event.isCancelled)
        return

    val victim = replaceMessage(event.player.name)
    val attacker = (event.damageSource.causingEntity as? Player)?.let { replaceMessage(it.name) }

    scope.launch {
        channel.createMessage(if (attacker != null)
            "**$victim** was killed by **$attacker**."
        else "**$victim** died.")
    }
}

private fun replaceMessage(input: String): String {
    return input.replace("@", "`@`")
        .replace("_", "\\_")
}