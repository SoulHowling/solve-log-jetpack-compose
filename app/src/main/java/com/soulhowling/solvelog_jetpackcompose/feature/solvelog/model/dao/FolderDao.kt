package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.dao

import androidx.room.Dao
import androidx.room.Query
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao: BaseDao<Folder> {

    @Query("SELECT * FROM folder WHERE id = :id")
    fun getFolder(id: Int): Flow<Folder?>

    @Query("SELECT * FROM folder")
    fun getAllFolders(): Flow<List<Folder>>
}