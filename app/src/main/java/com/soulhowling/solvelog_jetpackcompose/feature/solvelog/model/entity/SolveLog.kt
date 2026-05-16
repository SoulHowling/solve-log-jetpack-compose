package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "solve_log",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SolveLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val questionQuantity: Int,
    val selectionType: Int,
    val selectionQuantity: Int,
    val attemptCount: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val folderId: Int?
)