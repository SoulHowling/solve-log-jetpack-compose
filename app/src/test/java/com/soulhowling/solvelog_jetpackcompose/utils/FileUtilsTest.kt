package com.soulhowling.solvelog_jetpackcompose.utils

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.Memo
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class FileUtilsTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun generateSolveLogJson_containsLogAndQuestionsData() {
        val solveLog = SolveLog(
            id = 1,
            name = "Test Solve Log",
            questionQuantity = 10,
            answerFormat = 0,
            selectionType = 0,
            selectionQuantity = 4,
            attemptCount = 1,
            createdAt = 1000L,
            updatedAt = 2000L,
            folderId = 5,
            priority = 0,
        )
        val questions = listOf(
            Question(
                id = 101,
                solveLogId = 1,
                attempt = 1,
                questionIndex = 0,
                freeAnswerContent = "Ans1",
                selectedAnswer = 1,
                correctness = 1,
                isFlag = false,
            )
        )

        val jsonString = FileUtils.generateSolveLogJson(solveLog, questions)
        val json = JSONObject(jsonString)

        assertEquals(1, json.getInt("id"))
        assertEquals("Test Solve Log", json.getString("name"))
        assertEquals(10, json.getInt("questionQuantity"))

        val questionsArray = json.getJSONArray(FileUtils.KEY_QUESTIONS)
        assertEquals(1, questionsArray.length())
        assertEquals("Ans1", questionsArray.getJSONObject(0).getString("freeAnswerContent"))
    }

    @Test
    fun generateFolderJson_includesSubfoldersAndLogs() {
        val parentFolder = Folder(id = 1, name = "Parent", createdAt = 100L, updatedAt = 200L, parentFolderId = null)
        val childFolder = Folder(id = 2, name = "Child", createdAt = 100L, updatedAt = 200L, parentFolderId = 1)
        val allFolders = listOf(parentFolder, childFolder)

        val solveLog = SolveLog(id = 10, name = "Log in Parent", questionQuantity = 5, answerFormat = 0, selectionType = 0, selectionQuantity = 4, attemptCount = 1, createdAt = 100L, updatedAt = 200L, folderId = 1, priority = 0)
        val allLogs = listOf(solveLog)
        val questionsMap = mapOf(10 to emptyList<Question>())

        val jsonString = FileUtils.generateFolderJson(parentFolder, allFolders, allLogs, questionsMap)
        val json = JSONObject(jsonString)

        assertEquals("Parent", json.getString("name"))

        val logsArray = json.getJSONArray(FileUtils.KEY_SOLVE_LOGS)
        assertEquals(1, logsArray.length())

        val subfoldersArray = json.getJSONArray(FileUtils.KEY_SUBFOLDERS)
        assertEquals(1, subfoldersArray.length())
        assertEquals("Child", subfoldersArray.getJSONObject(0).getString("name"))
    }

    @Test
    fun generateAllDataJson_exportsFoldersAndLogs() {
        val folder = Folder(id = 1, name = "Folder 1", createdAt = 100L, updatedAt = 200L, parentFolderId = null)
        val solveLog = SolveLog(id = 10, name = "Log 1", questionQuantity = 5, answerFormat = 0, selectionType = 0, selectionQuantity = 4, attemptCount = 1, createdAt = 100L, updatedAt = 200L, folderId = 1, priority = 0)

        val jsonString = FileUtils.generateAllDataJson(listOf(folder), listOf(solveLog), emptyMap())
        val json = JSONObject(jsonString)

        val foldersArray = json.getJSONArray(FileUtils.KEY_FOLDERS)
        assertEquals(1, foldersArray.length())
        assertEquals("Folder 1", foldersArray.getJSONObject(0).getString("name"))

        val logsArray = json.getJSONArray(FileUtils.KEY_SOLVE_LOGS)
        assertEquals(1, logsArray.length())
        assertEquals("Log 1", logsArray.getJSONObject(0).getString("name"))
    }

    @Test
    fun generateMemoJson_and_parseMemoJson() {
        val memo = Memo(
            id = 5,
            name = "Test Memo",
            content = "Memo Content",
            createdAt = 1000L,
            updatedAt = 2000L,
            priority = 1,
            folderId = 3,
        )

        val jsonString = FileUtils.generateMemoJson(memo)
        val parsedMemo = FileUtils.parseMemoJson(JSONObject(jsonString))

        assertEquals(memo.id, parsedMemo.id)
        assertEquals(memo.name, parsedMemo.name)
        assertEquals(memo.content, parsedMemo.content)
        assertEquals(memo.priority, parsedMemo.priority)
        assertEquals(memo.folderId, parsedMemo.folderId)
    }

    @Test
    fun generateMemoFolderJson_includesSubfoldersAndMemos() {
        val folder = Folder(id = 1, name = "Memo Folder", createdAt = 100L, updatedAt = 200L, parentFolderId = null, priority = 0)
        val memo = Memo(id = 10, name = "Memo 10", content = "Content 10", createdAt = 100L, updatedAt = 200L, priority = 0, folderId = 1)

        val jsonString = FileUtils.generateMemoFolderJson(folder, listOf(folder), listOf(memo))
        val json = JSONObject(jsonString)

        assertEquals("Memo Folder", json.getString("name"))
        val memosArray = json.getJSONArray(FileUtils.KEY_MEMOS)
        assertEquals(1, memosArray.length())
        assertEquals("Memo 10", memosArray.getJSONObject(0).getString("name"))
    }

    @Test
    fun generateAllMemosDataJson_and_parseMemosJson() {
        val folder = Folder(id = 1, name = "Folder", createdAt = 100L, updatedAt = 200L, parentFolderId = null)
        val memo1 = Memo(id = 1, name = "Memo A", content = "Content A", createdAt = 100L, updatedAt = 200L, priority = 0, folderId = 1)
        val memo2 = Memo(id = 2, name = "Memo B", content = "Content B", createdAt = 100L, updatedAt = 200L, priority = 0, folderId = null)

        val jsonString = FileUtils.generateAllMemosDataJson(listOf(folder), listOf(memo1, memo2))
        val parsedMemos = FileUtils.parseMemosJson(jsonString)

        assertEquals(2, parsedMemos.size)
        assertEquals("Memo A", parsedMemos[0].name)
        assertEquals("Memo B", parsedMemos[1].name)
    }

    @Test
    fun parseMemosJson_handlesJsonArray() {
        val jsonArrayString = """
            [
                {"id": 1, "name": "Memo Array 1", "content": "Content 1"},
                {"id": 2, "name": "Memo Array 2", "content": "Content 2"}
            ]
        """.trimIndent()

        val list = FileUtils.parseMemosJson(jsonArrayString)
        assertEquals(2, list.size)
        assertEquals("Memo Array 1", list[0].name)
        assertEquals("Memo Array 2", list[1].name)
    }

    @Test
    fun parseMemosJson_handlesSingleJsonObject() {
        val jsonObjectString = """
            {"id": 99, "name": "Single Memo", "content": "Single Content"}
        """.trimIndent()

        val list = FileUtils.parseMemosJson(jsonObjectString)
        assertEquals(1, list.size)
        assertEquals("Single Memo", list[0].name)
    }

    @Test
    fun parseMemosJson_handlesInvalidJsonGracefully() {
        val invalidJson = "invalid json text"
        val list = FileUtils.parseMemosJson(invalidJson)
        assertTrue(list.isEmpty())
    }

    @Test
    fun writeAndReadTextFromUri() {
        val tempFile = File(context.cacheDir, "test_file_utils.txt")
        val uri = Uri.fromFile(tempFile)

        val success = FileUtils.writeTextToUri(context, uri, "Sample text content")
        assertTrue(success)

        val readText = FileUtils.readTextFromUri(context, uri)
        assertEquals("Sample text content", readText)

        tempFile.delete()
    }
}
