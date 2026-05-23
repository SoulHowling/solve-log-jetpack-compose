package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Upsert

interface BaseDao <T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: T)

    @Upsert
    suspend fun upsert(entity: T)

    @Delete
    suspend fun delete(entity: T)
}