package me.honkling.neopbb.lib

fun getCooldown(ms: Double): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (minutes > 0) append("$minutes minute${if (minutes != 1) "s" else ""} ")
        if (seconds > 0 || isEmpty()) append("$seconds second${if (seconds != 1) "s" else ""}")
    }.trim()
}


