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
        isCorrect: Boolean,
        isFlag: Boolean,
        solveLogId: Int
    ): Question {
        return Question(
            id = 0,
            attempt = attempt,
            questionIndex = questionIndex,
            selectedAnswer = selectedAnswer,
            isCorrect = isCorrect,
            isFlag = isFlag,
            solveLogId = solveLogId
        )
    }

    fun getQuestionBy(solveLogId: Int): Flow<List<Question>> {
        return questionDao.getQuestionBy(solveLogId)
    }

    suspend fun insertQuestion(question: Question) {
        questionDao.insert(question)
    }

    suspend fun upsertQuestion(question: Question) {
        questionDao.upsert(question)
    }

    suspend fun deleteQuestion(question: Question) {
        questionDao.delete(question)
    }
}