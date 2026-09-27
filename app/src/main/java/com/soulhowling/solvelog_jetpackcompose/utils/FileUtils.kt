package com.soulhowling.solvelog_jetpackcompose.utils

import android.content.Context
import android.net.Uri
import com.soulhowling.solvelog_jetpackcompose.core.database.DBKeys
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.Memo
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
    const val KEY_MEMOS = "memos"
    const val INDENT_SPACE = 4

    fun generateSolveLogJson(solveLog: SolveLog, questions: List<Question>): String {
        return solveLogToJsonObject(solveLog, questions).toString(INDENT_SPACE)
    }

    private fun solveLogToJsonObject(solveLog: SolveLog, questions: List<Question>): JSONObject {
        return JSONObject().apply {
            put(DBKeys.KEY_ID, solveLog.id)
            put(DBKeys.KEY_NAME, solveLog.name)
            put(DBKeys.KEY_QUESTION_QUANTITY, solveLog.questionQuantity)
            put(DBKeys.KEY_ANSWER_FORMAT, solveLog.answerFormat)
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
                    put(DBKeys.KEY_FREE_ANSWER_CONTENT, question.freeAnswerContent)
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

    fun generateMemoJson(memo: Memo): String {
        return memoToJsonObject(memo).toString(INDENT_SPACE)
    }

    private fun memoToJsonObject(memo: Memo): JSONObject {
        return JSONObject().apply {
            put(DBKeys.KEY_ID, memo.id)
            put(DBKeys.KEY_NAME, memo.name)
            put("content", memo.content)
            put(DBKeys.KEY_CREATED_AT, memo.createdAt)
            put(DBKeys.KEY_UPDATED_AT, memo.updatedAt)
            put(DBKeys.KEY_PRIORITY, memo.priority)
            if (memo.folderId != null) {
                put(DBKeys.KEY_FOLDER_ID, memo.folderId)
            }
        }
    }

    fun generateMemoFolderJson(
        folder: Folder,
        allFolders: List<Folder>,
        allMemos: List<Memo>
    ): String {
        return memoFolderToJsonObject(folder, allFolders, allMemos).toString(INDENT_SPACE)
    }

    private fun memoFolderToJsonObject(
        folder: Folder,
        allFolders: List<Folder>,
        allMemos: List<Memo>
    ): JSONObject {
        return JSONObject().apply {
            put(DBKeys.KEY_ID, folder.id)
            put(DBKeys.KEY_NAME, folder.name)
            put(DBKeys.KEY_CREATED_AT, folder.createdAt)
            put(DBKeys.KEY_UPDATED_AT, folder.updatedAt)
            put(DBKeys.KEY_PARENT_FOLDER_ID, folder.parentFolderId)
            put(DBKeys.KEY_PRIORITY, folder.priority)

            val memosArray = JSONArray()
            allMemos.filter { it.folderId == folder.id }.forEach { memo ->
                memosArray.put(memoToJsonObject(memo))
            }
            put(KEY_MEMOS, memosArray)

            val subfoldersArray = JSONArray()
            allFolders.filter { it.parentFolderId == folder.id }.forEach { subfolder ->
                subfoldersArray.put(memoFolderToJsonObject(subfolder, allFolders, allMemos))
            }
            put(KEY_SUBFOLDERS, subfoldersArray)
        }
    }

    fun generateAllMemosDataJson(folders: List<Folder>, memos: List<Memo>): String {
        val jsonObject = JSONObject().apply {
            val foldersArray = JSONArray()
            folders.forEach { folder ->
                foldersArray.put(JSONObject().apply {
                    put(DBKeys.KEY_ID, folder.id)
                    put(DBKeys.KEY_NAME, folder.name)
                    put(DBKeys.KEY_CREATED_AT, folder.createdAt)
                    put(DBKeys.KEY_UPDATED_AT, folder.updatedAt)
                    put(DBKeys.KEY_PARENT_FOLDER_ID, folder.parentFolderId)
                    put(DBKeys.KEY_PRIORITY, folder.priority)
                })
            }
            put(KEY_FOLDERS, foldersArray)

            val memosArray = JSONArray()
            memos.forEach { memo ->
                memosArray.put(memoToJsonObject(memo))
            }
            put(KEY_MEMOS, memosArray)
        }
        return jsonObject.toString(INDENT_SPACE)
    }

    fun parseMemoJson(jsonObject: JSONObject): Memo {
        val folderId = if (jsonObject.has(DBKeys.KEY_FOLDER_ID) && !jsonObject.isNull(DBKeys.KEY_FOLDER_ID)) {
            jsonObject.optInt(DBKeys.KEY_FOLDER_ID)
        } else {
            null
        }
        return Memo(
            id = jsonObject.optInt(DBKeys.KEY_ID, 0),
            name = jsonObject.optString(DBKeys.KEY_NAME, ""),
            content = jsonObject.optString("content", ""),
            createdAt = jsonObject.optLong(DBKeys.KEY_CREATED_AT, System.currentTimeMillis()),
            updatedAt = jsonObject.optLong(DBKeys.KEY_UPDATED_AT, System.currentTimeMillis()),
            priority = jsonObject.optInt(DBKeys.KEY_PRIORITY, 0),
            folderId = folderId
        )
    }

    fun parseMemosJson(jsonString: String): List<Memo> {
        val list = mutableListOf<Memo>()
        try {
            val trimmed = jsonString.trim()
            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    list.add(parseMemoJson(array.getJSONObject(i)))
                }
            } else {
                val jsonObject = JSONObject(trimmed)
                if (jsonObject.has(KEY_MEMOS)) {
                    val array = jsonObject.getJSONArray(KEY_MEMOS)
                    for (i in 0 until array.length()) {
                        list.add(parseMemoJson(array.getJSONObject(i)))
                    }
                } else if (jsonObject.has(DBKeys.KEY_NAME) || jsonObject.has("content")) {
                    list.add(parseMemoJson(jsonObject))
                }
            }
        } catch (e: Exception) {
            LogUtils.e("Failed to parse memo JSON: ${e.message}")
        }
        return list
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
