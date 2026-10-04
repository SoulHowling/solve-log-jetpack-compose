package com.soulhowling.solvelog_jetpackcompose.feature.memo.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.soulhowling.solvelog_jetpackcompose.core.database.DBKeys
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder

@Entity(
    tableName = DBKeys.TABLE_MEMO,
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = [DBKeys.KEY_ID],
            childColumns = [DBKeys.KEY_FOLDER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [DBKeys.KEY_FOLDER_ID])
    ]
)
data class Memo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val priority: Int = 0,
    val folderId: Int? = null
)
