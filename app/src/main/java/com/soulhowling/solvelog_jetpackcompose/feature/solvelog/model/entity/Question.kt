package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.Correctness

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
    val questionIndex: Int,
    val selectedAnswer: Int = -1,
    val correctness: Int = Correctness.NONE.index,
    val isFlag: Boolean = false,
    val solveLogId: Int
)
