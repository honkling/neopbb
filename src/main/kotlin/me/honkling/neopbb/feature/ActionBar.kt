@file:Feature("ActionBar")

package me.honkling.neopbb.feature

import me.honkling.commonlib.scheduler
import me.honkling.neopbb.instance
import me.honkling.neopbb.lib.onlinePlayers
import me.honkling.neopbb.profile.profile
import me.honkling.neopbb.wardenCooldown
import me.honkling.neopbb.wardens
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.text.DecimalFormat

private val cooldownFormat = DecimalFormat("#0.0")

private fun schedule(): Int {
    return scheduler.scheduleSyncRepeatingTask(instance, ::execute, 0L, 1L)
}

private fun execute() {
    val wardens = wardens

    for (player in onlinePlayers) {
        val profile = player.profile
        var displayWarden = Component.text("Warden: ")
            .color(NamedTextColor.GRAY)
        val displayMoney = Component.text("$${profile.displayMoney()}")
            .color(NamedTextColor.GREEN)

        for ((index, warden) in wardens.withIndex()) {
            displayWarden = displayWarden.append(
                if (warden == player)
                    Component.text("You!")
                        .color(NamedTextColor.GREEN)
                else warden.name()
                    .color(NamedTextColor.RED)
            )

            if (index + 1 < wardens.size)
                displayWarden = displayWarden.append(Component.text(", ")
                    .color(NamedTextColor.RED))
        }

        if (wardens.isEmpty())
            displayWarden = displayWarden.append(
                (if (wardenCooldown > 0) {
                    val cooldown = cooldownFormat.format(wardenCooldown / 20f)
                    Component.text("None! Please wait $cooldown seconds.")
                } else Component.text("None! Use /warden to become the warden."))
                    .color(NamedTextColor.RED))

        player.sendActionBar(Component.empty()
            .append(displayMoney)
            .append(Component.text(" || ")
                .color(NamedTextColor.DARK_GRAY))
            .append(displayWarden))
    }
}