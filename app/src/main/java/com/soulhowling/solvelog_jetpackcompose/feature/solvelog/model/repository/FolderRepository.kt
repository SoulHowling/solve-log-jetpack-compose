package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository

import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.dao.FolderDao
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import kotlinx.coroutines.flow.Flow

class FolderRepository(private val folderDao: FolderDao) {

    val allFolders: Flow<List<Folder>> = folderDao.getAllFolders()

    fun createFolder(
        name: String,
        parentFolderId: Int?,
    ): Folder {
        val createdAt = System.currentTimeMillis()
        return Folder(
            id = 0,
            name = name,
            createdAt = createdAt,
            updatedAt = createdAt,
            parentFolderId = parentFolderId
        )
    }

    suspend fun insertFolder(folder: Folder) {
        folderDao.insert(folder)
    }

    suspend fun upsertFolder(folder: Folder) {
        folderDao.upsert(folder)
    }

    suspend fun deleteFolder(folder: Folder) {
        folderDao.delete(folder)
    }
}