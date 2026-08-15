package com.soulhowling.solvelog_jetpackcompose.core.database

import androidx.room.TypeConverter
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerPhase

class Converters {
    @TypeConverter
    fun fromTimerPhase(value: TimerPhase): String {
        return value.name
    }

    @TypeConverter
    fun toTimerPhase(value: String): TimerPhase {
        return TimerPhase.valueOf(value)
    }
}
