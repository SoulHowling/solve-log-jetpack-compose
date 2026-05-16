package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "question",
    foreignKeys = [
        ForeignKey(
            entity = SolveLog::class,
            parentColumns = ["id"],
            childColumns = ["solveLogId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val attempt: Int,
    val selectedAnswer: Int,
    val isCorrect: Boolean,
    val isFlag: Boolean,
    val solveLogId: Int
)
