package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository

import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.dao.QuestionDao
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import kotlinx.coroutines.flow.Flow

class QuestionRepository(private val questionDao: QuestionDao) {

    val allQuestions: Flow<List<Question>> = questionDao.getAllQuestions()

    fun createQuestion(
        attempt: Int,
        selectedAnswer: Int,
        questionIndex: Int,
        correctness: Int,
        isFlag: Boolean,
        solveLogId: Int
    ): Question {
        return Question(
            id = 0,
            attempt = attempt,
            questionIndex = questionIndex,
            selectedAnswer = selectedAnswer,
            correctness = correctness,
            isFlag = isFlag,
            solveLogId = solveLogId
        )
    }

    fun getQuestionBy(solveLogId: Int): Flow<List<Question>> {
        return questionDao.getQuestionBy(solveLogId)
    }

    fun getQuestionBy(solveLogId: Int, attempt: Int): Flow<List<Question>> {
        return questionDao.getQuestionBy(solveLogId, attempt)
    }

    suspend fun getQuestionListBy(solveLogId: Int): List<Question> {
        return questionDao.getQuestionListBy(solveLogId)
    }

    suspend fun insertQuestion(question: Question) {
        questionDao.insert(question)
    }

    suspend fun insertQuestions(questions: List<Question>) {
        questionDao.insertAll(questions)
    }

    suspend fun upsertQuestion(question: Question) {
        questionDao.upsert(question)
    }

    suspend fun deleteQuestion(question: Question) {
        questionDao.delete(question)
    }

    suspend fun deleteQuestions(questions: List<Question>) {
        questionDao.deleteAll(questions)
    }

    suspend fun deleteQuestionByIndex(solveLogId: Int, questionIndex: Int) {
        questionDao.deleteQuestionByIndex(solveLogId, questionIndex)
    }

    suspend fun decrementQuestionIndices(solveLogId: Int, questionIndex: Int) {
        questionDao.decrementQuestionIndices(solveLogId, questionIndex)
    }

    suspend fun deleteQuestionsByAttempt(solveLogId: Int, attempt: Int) {
        questionDao.deleteQuestionsByAttempt(solveLogId, attempt)
    }

    suspend fun decrementAttemptIndices(solveLogId: Int, attempt: Int) {
        questionDao.decrementAttemptIndices(solveLogId, attempt)
    }

    suspend fun getQuestionsBySolveLogIds(solveLogIds: List<Int>): List<Question> {
        return questionDao.getQuestionsBySolveLogIds(solveLogIds)
    }

}
