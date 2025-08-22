package me.honkling.neopbb.lib

fun getCooldown(seconds: Long): String {
    val minutes = (seconds / 60).toInt()
    val seconds = (seconds % 60).toInt()

    return buildString {
        if (minutes > 0) append("$minutes minute${if (minutes != 1) "s" else ""} ")
        if (seconds > 0 || isEmpty()) append("$seconds second${if (seconds != 1) "s" else ""}")
    }.trim()
}


