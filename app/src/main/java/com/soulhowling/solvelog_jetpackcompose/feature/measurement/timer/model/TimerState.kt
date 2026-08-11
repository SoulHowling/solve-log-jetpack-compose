package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.soulhowling.solvelog_jetpackcompose.core.database.DBKeys

enum class TimerPhase {
    WORK, BREAK, COMPLETE
}

@Entity(tableName = DBKeys.TABLE_TIMER)
data class TimerState(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val workSet: Int,
    val workSeconds: Int,
    val breakSeconds: Int,
    val phase: TimerPhase = TimerPhase.WORK,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    @Ignore
    val initialSeconds: Int = 0,
    @Ignore
    val remainingSeconds: Int = 0,
    @Ignore
    val isRunning: Boolean = false
) {
    constructor(
        id: Int,
        title: String,
        workSet: Int,
        workSeconds: Int,
        breakSeconds: Int,
        phase: TimerPhase,
        createdAt: Long,
        updatedAt: Long
    ) : this(
        id = id,
        title = title,
        workSet = workSet,
        workSeconds = workSeconds,
        breakSeconds = breakSeconds,
        phase = phase,
        createdAt = createdAt,
        updatedAt = updatedAt,
        initialSeconds = workSeconds,
        remainingSeconds = workSeconds,
        isRunning = false
    )

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
                        isRunning = true
                    )
                } else {
                    this.copy(
                        phase = TimerPhase.COMPLETE,
                        workSet = 0,
                        remainingSeconds = 0,
                        isRunning = false
                    )
                }
            }
            TimerPhase.BREAK -> {
                this.copy(
                    phase = TimerPhase.WORK,
                    initialSeconds = workSeconds,
                    remainingSeconds = workSeconds,
                    isRunning = true
                )
            }
            TimerPhase.COMPLETE -> {
                this
            }
        }
    }
}
