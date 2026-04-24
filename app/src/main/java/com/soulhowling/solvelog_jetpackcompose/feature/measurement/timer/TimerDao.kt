package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao {
    @Query("SELECT * FROM timer_state WHERE id = :id")
    fun getTimerState(id: Int): Flow<TimerState?>

    @Query("SELECT * FROM timer_state")
    fun getAllTimerStates(): Flow<List<TimerState>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimerState(timerState: TimerState)

    @Upsert
    suspend fun upsertTimerState(timerState: TimerState)

    @Query("DELETE FROM timer_state WHERE id = :id")
    suspend fun deleteTimerState(id: Int)
}