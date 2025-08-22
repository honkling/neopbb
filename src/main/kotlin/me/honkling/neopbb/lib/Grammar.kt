package me.honkling.neopbb.lib

fun getOrdinal(value: Int): String {
    return value.toString() + when (value.mod(10)) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}