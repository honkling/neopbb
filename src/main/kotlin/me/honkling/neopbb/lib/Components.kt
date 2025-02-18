package me.honkling.neopbb.lib

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

fun Component.plainText(): String {
    return PlainTextComponentSerializer.plainText().serialize(this)
}