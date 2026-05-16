package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folder")
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
)
