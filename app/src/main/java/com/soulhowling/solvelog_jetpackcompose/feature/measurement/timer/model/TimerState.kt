package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TimerPhase {
    WORK, BREAK, COMPLETE
}

@Entity(tableName = "timer_state")
data class TimerState(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val workSet: Int,
    val workSeconds: Int,
    val breakSeconds: Int,
    val phase: TimerPhase = TimerPhase.WORK,
    val initialSeconds: Int,
    val remainingSeconds: Int,
    val isRunning: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
) {

    fun tick(): TimerState {
        return if (remainingSeconds > 0) {
            this.copy(
                remainingSeconds = remainingSeconds - 1,
                lastModified = System.currentTimeMillis()
            )
        } else {
            this
        }
    }

    fun nextPhase(): TimerState {
        return when (phase) {
            TimerPhase.WORK -> {
                val remainingWork = workSet - 1
                if (remainingWork > 0) {
                    this.copy(
                        phase = TimerPhase.BREAK,
                        workSet = remainingWork,
                        initialSeconds = breakSeconds,
                        remainingSeconds = breakSeconds,
                        isRunning = true,
                        lastModified = System.currentTimeMillis()
                    )
                } else {
                    this.copy(
                        phase = TimerPhase.COMPLETE,
                        workSet = 0,
                        remainingSeconds = 0,
                        isRunning = false,
                        lastModified = System.currentTimeMillis()
                    )
                }
            }
            TimerPhase.BREAK -> {
                this.copy(
                    phase = TimerPhase.WORK,
                    initialSeconds = workSeconds,
                    remainingSeconds = workSeconds,
                    isRunning = true,
                    lastModified = System.currentTimeMillis()
                )
            }
            TimerPhase.COMPLETE -> {
                this
            }
        }
    }
}
