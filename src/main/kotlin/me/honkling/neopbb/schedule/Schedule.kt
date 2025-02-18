package me.honkling.neopbb.schedule

import me.honkling.neopbb.schedule.Period.*

val scheduleHours = mapOf(
    0..<6 to LightsOut,
    6..<8 to RollCall,
    8..<10 to Breakfast,
    10..<13 to FreeTime,
    13..<16 to JobTime,
    16..<19 to Lunch,
    19..<21 to RollCall,
    21..<22 to CellTime,
    22..24 to LightsOut
)