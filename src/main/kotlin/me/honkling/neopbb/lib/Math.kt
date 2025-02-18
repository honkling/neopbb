package me.honkling.neopbb.lib

fun Float.clamp(min: Float, max: Float): Float {
    return if (this < min) min
           else if (this > max) max
           else this
}