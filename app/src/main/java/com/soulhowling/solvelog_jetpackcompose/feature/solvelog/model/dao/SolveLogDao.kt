package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SolveLogDao: BaseDao<SolveLog> {

    @Query("SELECT * FROM solve_log WHERE id = :id")
    fun getSolveLog(id: Int): Flow<SolveLog?>
    
    @Query("SELECT * FROM solve_log WHERE folderId = :folderId ORDER BY priority ASC, createdAt ASC")
    suspend fun getSolveLogListBy(folderId: Int): List<SolveLog>

    @Query("SELECT * FROM solve_log ORDER BY priority ASC, createdAt ASC")
    fun getAllSolveLogs(): Flow<List<SolveLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAndReturnId(solveLog: SolveLog): Long

    @Query("DELETE FROM solve_log")
    override suspend fun deleteAll()
}
