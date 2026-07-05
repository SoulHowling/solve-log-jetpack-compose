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

    @Query("SELECT * FROM question WHERE solveLogId = :solveLogId")
    suspend fun getQuestionListBy(solveLogId: Int): List<Question>

    @Query("SELECT * FROM question WHERE solveLogId = :solveLogId AND attempt = :attempt ORDER BY questionIndex ASC")
    fun getQuestionBy(solveLogId: Int, attempt: Int): Flow<List<Question>>

    @Query("DELETE FROM question WHERE solveLogId = :solveLogId AND questionIndex = :questionIndex")
    suspend fun deleteQuestionByIndex(solveLogId: Int, questionIndex: Int)

    @Query("UPDATE question SET questionIndex = questionIndex - 1 WHERE solveLogId = :solveLogId AND questionIndex > :questionIndex")
    suspend fun decrementQuestionIndices(solveLogId: Int, questionIndex: Int)

    @Query("DELETE FROM question WHERE solveLogId = :solveLogId AND attempt = :attempt")
    suspend fun deleteQuestionsByAttempt(solveLogId: Int, attempt: Int)

    @Query("UPDATE question SET attempt = attempt - 1 WHERE solveLogId = :solveLogId AND attempt > :attempt")
    suspend fun decrementAttemptIndices(solveLogId: Int, attempt: Int)

    @Query("SELECT * FROM question WHERE solveLogId IN (:solveLogIds)")
    suspend fun getQuestionsBySolveLogIds(solveLogIds: List<Int>): List<Question>
}
