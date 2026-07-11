package com.soulhowling.solvelog_jetpackcompose.utils

import android.content.Context
import android.net.Uri
import com.soulhowling.solvelog_jetpackcompose.core.database.DBKeys
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import org.json.JSONArray
import org.json.JSONObject

object FileUtils {

    const val KEY_SOLVE_LOGS = "solveLogs"
    const val KEY_SUBFOLDERS = "subfolders"
    const val KEY_QUESTIONS = "questions"
    const val KEY_FOLDERS = "folders"
    const val INDENT_SPACE = 4

    fun generateSolveLogJson(solveLog: SolveLog, questions: List<Question>): String {
        return solveLogToJsonObject(solveLog, questions).toString(INDENT_SPACE)
    }

    private fun solveLogToJsonObject(solveLog: SolveLog, questions: List<Question>): JSONObject {
        return JSONObject().apply {
            put(DBKeys.KEY_ID, solveLog.id)
            put(DBKeys.KEY_NAME, solveLog.name)
            put(DBKeys.KEY_QUESTION_QUANTITY, solveLog.questionQuantity)
            put(DBKeys.KEY_SELECTION_TYPE, solveLog.selectionType)
            put(DBKeys.KEY_SELECTION_QUANTITY, solveLog.selectionQuantity)
            put(DBKeys.KEY_ATTEMPT_COUNT, solveLog.attemptCount)
            put(DBKeys.KEY_CREATED_AT, solveLog.createdAt)
            put(DBKeys.KEY_UPDATED_AT, solveLog.updatedAt)
            put(DBKeys.KEY_FOLDER_ID, solveLog.folderId)

            val questionsArray = JSONArray()
            questions.forEach { question ->
                questionsArray.put(JSONObject().apply {
                    put(DBKeys.KEY_ID, question.id)
                    put(DBKeys.KEY_ATTEMPT, question.attempt)
                    put(DBKeys.KEY_QUESTION_INDEX, question.questionIndex)
                    put(DBKeys.KEY_SELECTED_ANSWER, question.selectedAnswer)
                    put(DBKeys.KEY_CORRECTNESS, question.correctness)
                    put(DBKeys.KEY_IS_FLAG, question.isFlag)
                })
            }
            put(KEY_QUESTIONS, questionsArray)
        }
    }

    fun generateFolderJson(
        folder: Folder,
        allFolders: List<Folder>,
        allLogs: List<SolveLog>,
        logQuestionsMap: Map<Int, List<Question>>
    ): String {
        return folderToJsonObject(folder, allFolders, allLogs, logQuestionsMap).toString(INDENT_SPACE)
    }

    private fun folderToJsonObject(
        folder: Folder,
        allFolders: List<Folder>,
        allLogs: List<SolveLog>,
        logQuestionsMap: Map<Int, List<Question>>
    ): JSONObject {
        return JSONObject().apply {
            put(DBKeys.KEY_ID, folder.id)
            put(DBKeys.KEY_NAME, folder.name)
            put(DBKeys.KEY_CREATED_AT, folder.createdAt)
            put(DBKeys.KEY_UPDATED_AT, folder.updatedAt)
            put(DBKeys.KEY_PARENT_FOLDER_ID, folder.parentFolderId)

            val logsArray = JSONArray()
            allLogs.filter { it.folderId == folder.id }.forEach { log ->
                val questions = logQuestionsMap[log.id] ?: emptyList()
                logsArray.put(solveLogToJsonObject(log, questions))
            }
            put(KEY_SOLVE_LOGS, logsArray)

            val subfoldersArray = JSONArray()
            allFolders.filter { it.parentFolderId == folder.id }.forEach { subfolder ->
                subfoldersArray.put(folderToJsonObject(subfolder, allFolders, allLogs, logQuestionsMap))
            }
            put(KEY_SUBFOLDERS, subfoldersArray)
        }
    }

    fun generateAllDataJson(
        folders: List<Folder>,
        logs: List<SolveLog>,
        logQuestionsMap: Map<Int, List<Question>>
    ): String {
        val jsonObject = JSONObject().apply {
            val foldersArray = JSONArray()
            folders.forEach { folder ->
                foldersArray.put(JSONObject().apply {
                    put(DBKeys.KEY_ID, folder.id)
                    put(DBKeys.KEY_NAME, folder.name)
                    put(DBKeys.KEY_CREATED_AT, folder.createdAt)
                    put(DBKeys.KEY_UPDATED_AT, folder.updatedAt)
                    put(DBKeys.KEY_PARENT_FOLDER_ID, folder.parentFolderId)
                })
            }
            put(KEY_FOLDERS, foldersArray)

            val logsArray = JSONArray()
            logs.forEach { log ->
                val questions = logQuestionsMap[log.id] ?: emptyList()
                logsArray.put(solveLogToJsonObject(log, questions))
            }
            put(KEY_SOLVE_LOGS, logsArray)
        }
        return jsonObject.toString(INDENT_SPACE)
    }

    fun writeTextToUri(context: Context, uri: Uri, text: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(text.toByteArray(Charsets.UTF_8))
                true
            } ?: false
        } catch (e: Exception) {
            LogUtils.e("Failed to write text to Uri: ${e.message}")
            false
        }
    }

    fun readTextFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            LogUtils.e("Failed to read text from Uri: ${e.message}")
            null
        }
    }
}
