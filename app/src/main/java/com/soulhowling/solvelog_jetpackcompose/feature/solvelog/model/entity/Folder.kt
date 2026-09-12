package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.soulhowling.solvelog_jetpackcompose.core.database.DBKeys

@Entity(
    tableName = DBKeys.TABLE_FOLDER,
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = [DBKeys.KEY_ID],
            childColumns = [DBKeys.KEY_PARENT_FOLDER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [DBKeys.KEY_PARENT_FOLDER_ID])
    ]
)
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val parentFolderId: Int?,
    @ColumnInfo(defaultValue = "0")
    val priority: Int = 0
)
