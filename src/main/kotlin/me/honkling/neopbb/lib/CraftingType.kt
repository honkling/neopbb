package me.honkling.neopbb.lib

import me.honkling.neopbb.profile.type.EnumDataType

val craftingTypeType = EnumDataType(CraftingType::class.java)
enum class CraftingType {
    Shaped,
    Shapeless
}