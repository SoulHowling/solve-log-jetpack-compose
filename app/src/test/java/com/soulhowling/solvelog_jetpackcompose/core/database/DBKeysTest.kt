package com.soulhowling.solvelog_jetpackcompose.core.database

import org.junit.Assert.assertEquals
import org.junit.Test

class DBKeysTest {

    @Test
    fun tableNames_areCorrect() {
        assertEquals("solve_log", DBKeys.TABLE_SOLVE_LOG)
        assertEquals("question", DBKeys.TABLE_QUESTION)
        assertEquals("folder", DBKeys.TABLE_FOLDER)
        assertEquals("timer_state", DBKeys.TABLE_TIMER)
        assertEquals("memo", DBKeys.TABLE_MEMO)
    }

    @Test
    fun commonKeys_areCorrect() {
        assertEquals("id", DBKeys.KEY_ID)
    }

    @Test
    fun solveLogKeys_areCorrect() {
        assertEquals("name", DBKeys.KEY_NAME)
        assertEquals("questionQuantity", DBKeys.KEY_QUESTION_QUANTITY)
        assertEquals("answerFormat", DBKeys.KEY_ANSWER_FORMAT)
        assertEquals("selectionType", DBKeys.KEY_SELECTION_TYPE)
        assertEquals("selectionQuantity", DBKeys.KEY_SELECTION_QUANTITY)
        assertEquals("attemptCount", DBKeys.KEY_ATTEMPT_COUNT)
        assertEquals("createdAt", DBKeys.KEY_CREATED_AT)
        assertEquals("updatedAt", DBKeys.KEY_UPDATED_AT)
        assertEquals("folderId", DBKeys.KEY_FOLDER_ID)
        assertEquals("priority", DBKeys.KEY_PRIORITY)
    }

    @Test
    fun questionKeys_areCorrect() {
        assertEquals("attempt", DBKeys.KEY_ATTEMPT)
        assertEquals("questionIndex", DBKeys.KEY_QUESTION_INDEX)
        assertEquals("freeAnswerContent", DBKeys.KEY_FREE_ANSWER_CONTENT)
        assertEquals("selectedAnswer", DBKeys.KEY_SELECTED_ANSWER)
        assertEquals("correctness", DBKeys.KEY_CORRECTNESS)
        assertEquals("isFlag", DBKeys.KEY_IS_FLAG)
        assertEquals("solveLogId", DBKeys.KEY_SOLVE_LOG_ID)
    }

    @Test
    fun folderKeys_areCorrect() {
        assertEquals("parentFolderId", DBKeys.KEY_PARENT_FOLDER_ID)
    }
}
