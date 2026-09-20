package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.soulhowling.solvelog_jetpackcompose.core.database.DBKeys
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.Correctness

@Entity(
    tableName = DBKeys.TABLE_QUESTION,
    foreignKeys = [
        ForeignKey(
            entity = SolveLog::class,
            parentColumns = [DBKeys.KEY_ID],
            childColumns = [DBKeys.KEY_SOLVE_LOG_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [DBKeys.KEY_SOLVE_LOG_ID])
    ]
)
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val attempt: Int,
    val questionIndex: Int,
    val freeAnswerContent: String?,
    val selectedAnswer: Int = -1,
    val correctness: Int = Correctness.NONE.index,
    val isFlag: Boolean = false,
    val solveLogId: Int
)
