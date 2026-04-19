package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_state")
data class TimerState(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val initialSeconds: Int,
    val remainingSeconds: Int,
    val isRunning: Boolean = false
) {
    fun isTicking(): Boolean {
        return  isRunning && remainingSeconds > 0
    }

    fun hasTimeLeft(): Boolean {
        return remainingSeconds > 0
    }

    fun isFinished(): Boolean {
        return remainingSeconds == 0
    }

    fun tick(): TimerState {
        return if (remainingSeconds > 0) {
            this.copy(remainingSeconds = remainingSeconds - 1)
        } else {
            this
        }
    }

}