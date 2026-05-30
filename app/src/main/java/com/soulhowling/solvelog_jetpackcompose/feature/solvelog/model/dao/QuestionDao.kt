package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.dao

import androidx.room.Dao
import androidx.room.Query
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao: BaseDao<Question> {

    @Query("SELECT * FROM question WHERE id = :id")
    fun getQuestion(id: Int): Flow<Question?>

    @Query("SELECT * FROM question")
    fun getAllQuestions(): Flow<List<Question>>

    @Query("SELECT * FROM question WHERE solveLogId = :solveLogId")
    fun getQuestionBy(solveLogId: Int): Flow<List<Question>>
}