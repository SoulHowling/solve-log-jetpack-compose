package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model

import androidx.room.Entity
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
)
