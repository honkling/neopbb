package me.honkling.neopbb.profile

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

enum class Role(val isAuthority: Boolean, val color: TextColor, val nameColor: TextColor = NamedTextColor.GRAY) {
    Prisoner(false, NamedTextColor.GOLD),
    Escapee(false, NamedTextColor.RED),
    Nurse(true, NamedTextColor.LIGHT_PURPLE),
    Guard(true, NamedTextColor.BLUE),
    RiotGuard(true, NamedTextColor.DARK_GRAY),
    Warden(true, NamedTextColor.RED, NamedTextColor.WHITE)
}