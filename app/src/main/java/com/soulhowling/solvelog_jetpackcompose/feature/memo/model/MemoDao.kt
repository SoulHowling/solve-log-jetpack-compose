package com.soulhowling.solvelog_jetpackcompose.feature.memo.model

import androidx.room.Dao
import androidx.room.Query
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.dao.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao: BaseDao<Memo> {
    @Query("SELECT * FROM memo WHERE id = :id")
    fun getMemo(id: Int): Flow<Memo?>

    @Query("SELECT * FROM memo ORDER BY priority ASC, createdAt ASC")
    fun getAllMemos(): Flow<List<Memo>>

    @Query("SELECT MAX(priority) FROM memo")
    suspend fun getMaxPriority(): Int?

    @Query("DELETE FROM memo")
    suspend fun deleteAll()
}
