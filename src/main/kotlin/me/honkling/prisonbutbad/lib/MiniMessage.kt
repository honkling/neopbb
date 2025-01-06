package me.honkling.prisonbutbad.lib

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

val miniMessage = MiniMessage.builder()
    .tags(TagResolver.resolver(
        TagResolver.standard(),
        TagResolver.resolver("good", Tag.styling(TextColor.color(0x8F, 0xF7, 0x9B))),
        TagResolver.resolver("good2", Tag.styling(TextColor.color(0xBF, 0xFF, 0xC6))),
        TagResolver.resolver("bad", Tag.styling(TextColor.color(0xFF, 0x6E, 0x6E))),
        TagResolver.resolver("bad2", Tag.styling(TextColor.color(0xFF, 0x7F, 0x6E))),
        TagResolver.resolver("useful", Tag.styling(TextColor.color(0x00, 0x71, 0xBC))),
        TagResolver.resolver("useful2", Tag.styling(TextColor.color(0x15, 0x8C, 0xDB))),
        Placeholder.parsed("success", "<good>✔ "),
        Placeholder.parsed("failure", "<bad>⚠ <#bad2>"),
        Placeholder.parsed("info", "<useful>ⓘ ")
    ))
    .build()

val String.mm
    get() = miniMessage.deserialize(this)
        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)