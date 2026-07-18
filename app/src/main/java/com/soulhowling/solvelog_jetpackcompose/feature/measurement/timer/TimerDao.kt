package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer

import androidx.room.Dao
import androidx.room.Query
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.dao.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao: BaseDao<TimerState> {
    @Query("SELECT * FROM timer_state WHERE id = :id")
    fun getTimerState(id: Int): Flow<TimerState?>

    @Query("SELECT * FROM timer_state")
    fun getAllTimerStates(): Flow<List<TimerState>>

    @Query("DELETE FROM timer_state WHERE id = :id")
    suspend fun deleteTimerState(id: Int)

    @Query("DELETE FROM timer_state")
    override suspend fun deleteAll()
}