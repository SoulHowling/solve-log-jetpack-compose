package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.AnswerFormat
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class QuestionRepositoryTest {

    private lateinit var database: SolveLogDatabase
    private lateinit var repository: QuestionRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SolveLogDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = QuestionRepository(database.questionDao())
        
        // Insert a dummy SolveLog to satisfy Foreign Key constraints
        runTest {
            database.solveLogDao().insert(
                com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog(
                    id = 1,
                    name = "Dummy Log",
                    questionQuantity = 10,
                    answerFormat = AnswerFormat.MULTIPLE_CHOICE.index,
                    selectionType = 0,
                    selectionQuantity = 4,
                    attemptCount = 1,
                    createdAt = 0,
                    updatedAt = 0,
                    folderId = null
                )
            )
            database.solveLogDao().insert(
                com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog(
                    id = 2,
                    name = "Dummy Log 2",
                    questionQuantity = 10,
                    answerFormat = AnswerFormat.MULTIPLE_CHOICE.index,
                    selectionType = 0,
                    selectionQuantity = 4,
                    attemptCount = 1,
                    createdAt = 0,
                    updatedAt = 0,
                    folderId = null
                )
            )
            database.solveLogDao().insert(
                com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog(
                    id = 3,
                    name = "Dummy Log 3",
                    questionQuantity = 10,
                    answerFormat = AnswerFormat.MULTIPLE_CHOICE.index,
                    selectionType = 0,
                    selectionQuantity = 4,
                    attemptCount = 1,
                    createdAt = 0,
                    updatedAt = 0,
                    folderId = null
                )
            )
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `createQuestion should return question with id 0`() {
        val question = repository.createQuestion(
            attempt = 1,
            selectedAnswer = 2,
            questionIndex = 0,
            freeAnswerContent = "Test",
            correctness = 1,
            isFlag = true,
            solveLogId = 100
        )
        
        assertEquals(0, question.id)
        assertEquals(1, question.attempt)
        assertEquals(2, question.selectedAnswer)
        assertEquals("Test", question.freeAnswerContent)
        assertEquals(100, question.solveLogId)
        assertTrue(question.isFlag)
    }

    @Test
    fun `decrementQuestionIndices should shift indices down for questions after deleted index`() = runTest {
        val solveLogId = 1
        val questions = listOf(
            Question(attempt = 1, questionIndex = 0, freeAnswerContent = null, solveLogId = solveLogId),
            Question(attempt = 1, questionIndex = 1, freeAnswerContent = null, solveLogId = solveLogId),
            Question(attempt = 1, questionIndex = 2, freeAnswerContent = null, solveLogId = solveLogId),
            Question(attempt = 2, questionIndex = 1, freeAnswerContent = null, solveLogId = solveLogId) // Different attempt, same log
        )
        repository.insertQuestions(questions)
        
        // Delete index 1
        repository.deleteQuestionByIndex(solveLogId, 1)
        // Shifting: index 2 -> 1
        repository.decrementQuestionIndices(solveLogId, 1)
        
        val result = repository.getQuestionListBy(solveLogId)
        // Original 0 stays 0
        // Original 1 (att 1 and 2) are deleted by index? 
        // Wait, deleteQuestionByIndex deletes ALL attempts for that index in that log?
        // Let's check DAO: DELETE FROM question WHERE solveLogId = :solveLogId AND questionIndex = :questionIndex
        // Yes, it deletes for all attempts.
        
        assertEquals(2, result.size)
        assertTrue(result.any { it.questionIndex == 0 && it.attempt == 1 })
        assertTrue(result.any { it.questionIndex == 1 && it.attempt == 1 }) // Original index 2
        assertFalse(result.any { it.questionIndex == 2 })
    }

    @Test
    fun `decrementAttemptIndices should shift attempt numbers down`() = runTest {
        val solveLogId = 1
        val questions = listOf(
            Question(attempt = 1, questionIndex = 0, freeAnswerContent = null, solveLogId = solveLogId),
            Question(attempt = 2, questionIndex = 0, freeAnswerContent = null, solveLogId = solveLogId),
            Question(attempt = 3, questionIndex = 0, freeAnswerContent = null, solveLogId = solveLogId)
        )
        repository.insertQuestions(questions)
        
        // Delete attempt 2
        repository.deleteQuestionsByAttempt(solveLogId, 2)
        // Shifting: attempt 3 -> 2
        repository.decrementAttemptIndices(solveLogId, 2)
        
        val result = repository.getQuestionListBy(solveLogId)
        assertEquals(2, result.size)
        assertTrue(result.any { it.attempt == 1 })
        assertTrue(result.any { it.attempt == 2 }) // Original attempt 3
        assertFalse(result.any { it.attempt == 3 })
    }

    @Test
    fun `getQuestionsBySolveLogIds should return questions for multiple logs`() = runTest {
        repository.insertQuestions(listOf(
            Question(attempt = 1, questionIndex = 0, freeAnswerContent = null, solveLogId = 1),
            Question(attempt = 1, questionIndex = 0, freeAnswerContent = null, solveLogId = 2),
            Question(attempt = 1, questionIndex = 0, freeAnswerContent = null, solveLogId = 3)
        ))
        
        val result = repository.getQuestionsBySolveLogIds(listOf(1, 3))
        assertEquals(2, result.size)
        assertTrue(result.all { it.solveLogId == 1 || it.solveLogId == 3 })
    }
}
