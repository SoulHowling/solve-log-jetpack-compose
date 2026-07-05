package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.Correctness
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.FolderRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.QuestionRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.SolveLogRepository
import com.soulhowling.solvelog_jetpackcompose.utils.FileUtils
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class SolveLogListItem {
    data class FolderItem(val folder: Folder) : SolveLogListItem()
    data class LogItem(val log: SolveLog) : SolveLogListItem()
}

sealed class ExportTarget : java.io.Serializable {
    data class Log(val id: Int, val name: String) : ExportTarget()
    data class Folder(val id: Int, val name: String) : ExportTarget()
    object All : ExportTarget()

    val fileName: String
        get() {
            val baseName = when (this) {
                is Log -> name
                is Folder -> name
                is All -> "solve_log_backup"
            }
            val safeName = baseName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            return "$safeName.json"
        }
}

class SolveLogListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as SolveLogApplication).database
    private val folderRepository: FolderRepository = FolderRepository(database.folderDao())
    private val solveLogRepository: SolveLogRepository = SolveLogRepository(database.solveLogDao())
    private val questionRepository: QuestionRepository = QuestionRepository(database.questionDao())

    private val _currentFolderId = MutableStateFlow<Int?>(null)

    private val allFolders = folderRepository.allFolders
    private val allSolveLogs = solveLogRepository.allSolveLogs

    private val _exportTarget = MutableSharedFlow<ExportTarget>()
    val exportTarget = _exportTarget.asSharedFlow()

    val folders: StateFlow<List<Folder>> = allFolders.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val displayItems: StateFlow<List<SolveLogListItem>> = combine(
        allFolders,
        allSolveLogs,
        _currentFolderId
    ) { folders, logs, currentId ->
        val folderItems = folders
            .filter { it.parentFolderId == currentId }
            .map { SolveLogListItem.FolderItem(it) }
        val logItems = logs
            .filter { it.folderId == currentId }
            .map { SolveLogListItem.LogItem(it) }
        folderItems + logItems
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentFolderPath: StateFlow<String?> = combine(
        allFolders,
        _currentFolderId
    ) { folders, currentId ->
        if (currentId == null) return@combine null

        val path = mutableListOf<String>()
        val visited = mutableSetOf<Int>()
        var folder = folders.find { it.id == currentId }
        while (folder != null && visited.add(folder.id)) {
            path.add(0, folder.name)
            folder = folders.find { it.id == folder.parentFolderId }
        }
        path.joinToString(" / ")
    }.stateIn(viewModelScope, WhileSubscribed(5000), null)

    fun selectFolder(folderId: Int?) {
        _currentFolderId.value = folderId
    }

    fun navigateBack() {
        val currentId = _currentFolderId.value ?: return
        viewModelScope.launch {
            val currentFolder = folders.first().find { it.id == currentId }
            _currentFolderId.value = currentFolder?.parentFolderId
        }
    }

    fun insertSolveLog(
        name: String,
        questionQuantity: Int,
        selectionType: SelectionType,
        selectionQuantity: SelectionQuantity
    ) {
        viewModelScope.launch {
            database.withTransaction {
                val solveLog = solveLogRepository.createSolveLog(
                    name = name,
                    questionQuantity = questionQuantity,
                    selectionType = selectionType,
                    selectionQuantity = selectionQuantity,
                    folderId = _currentFolderId.value
                )
                val solveLogId = solveLogRepository.insertAndReturnId(solveLog)

                val questions = (0 until questionQuantity).map { index ->
                    questionRepository.createQuestion(
                        attempt = 1,
                        selectedAnswer = -1,
                        questionIndex = index,
                        correctness = Correctness.NONE.index,
                        isFlag = false,
                        solveLogId = solveLogId
                    )
                }
                questionRepository.insertQuestions(questions)
            }
        }
    }

    fun deleteSolveLog(solveLog: SolveLog) {
        viewModelScope.launch {
            solveLogRepository.deleteSolveLog(solveLog)
        }
    }

    fun insertFolder(name: String) {
        viewModelScope.launch {
            val folder = folderRepository.createFolder(
                name = name,
                parentFolderId = _currentFolderId.value
            )
            folderRepository.insertFolder(folder)
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            folderRepository.deleteFolder(folder)
        }
    }

    fun moveSolveLog(solveLog: SolveLog, targetFolderId: Int?) {
        viewModelScope.launch {
            val updatedLog = solveLog.copy(
                folderId = targetFolderId,
                updatedAt = System.currentTimeMillis()
            )
            solveLogRepository.upsertSolveLog(updatedLog)
        }
    }

    fun moveFolder(folder: Folder, targetFolderId: Int?) {
        viewModelScope.launch {
            if (folder.id == targetFolderId) return@launch

            if (targetFolderId != null) {
                var currentParentId: Int? = targetFolderId
                val folders = allFolders.first()
                val visited = mutableSetOf<Int>()
                while (currentParentId != null && visited.add(currentParentId)) {
                    if (currentParentId == folder.id) return@launch
                    currentParentId = folders.find { it.id == currentParentId }?.parentFolderId
                }
            }

            val updatedFolder = folder.copy(
                parentFolderId = targetFolderId,
                updatedAt = System.currentTimeMillis()
            )
            folderRepository.upsertFolder(updatedFolder)
        }
    }

    fun export(solveLog: SolveLog) {
        viewModelScope.launch {
            _exportTarget.emit(ExportTarget.Log(solveLog.id, solveLog.name))
        }
    }

    fun export(folder: Folder) {
        viewModelScope.launch {
            _exportTarget.emit(ExportTarget.Folder(folder.id, folder.name))
        }
    }

    fun exportAll() {
        viewModelScope.launch {
            _exportTarget.emit(ExportTarget.All)
        }
    }

    fun performExport(uri: android.net.Uri, target: ExportTarget, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val jsonString = when (target) {
                        is ExportTarget.Log -> {
                            val solveLog = solveLogRepository.getSolveLog(target.id).first() ?: return@withContext false
                            val questions = questionRepository.getQuestionListBy(target.id)
                            FileUtils.generateSolveLogJson(solveLog, questions)
                        }
                        is ExportTarget.Folder -> {
                            val allFolders = folderRepository.allFolders.first()
                            val folder = allFolders.find { it.id == target.id } ?: return@withContext false
                            val allLogs = solveLogRepository.allSolveLogs.first()

                            val descendantFolderIds = mutableSetOf<Int>()
                            fun collectFolderIds(id: Int) {
                                if (descendantFolderIds.add(id)) {
                                    allFolders.filter { it.parentFolderId == id }.forEach { collectFolderIds(it.id) }
                                }
                            }
                            collectFolderIds(target.id)

                            val logsToExport = allLogs.filter { it.folderId in descendantFolderIds }
                            val logIds = logsToExport.map { it.id }
                            val allQuestions = questionRepository.getQuestionsBySolveLogIds(logIds)
                            val logQuestionsMap = allQuestions.groupBy { it.solveLogId }

                            FileUtils.generateFolderJson(folder, allFolders, allLogs, logQuestionsMap)
                        }
                        is ExportTarget.All -> {
                            val folders = folderRepository.allFolders.first()
                            val logs = solveLogRepository.allSolveLogs.first()

                            val logIds = logs.map { it.id }
                            val allQuestions = questionRepository.getQuestionsBySolveLogIds(logIds)
                            val logQuestionsMap = allQuestions.groupBy { it.solveLogId }

                            FileUtils.generateAllDataJson(folders, logs, logQuestionsMap)
                        }
                    }
                    FileUtils.writeTextToUri(getApplication(), uri, jsonString)
                } catch (e: Exception) {
                    LogUtils.e("Export failed", e)
                    false
                }
            }
            onResult(success)
        }
    }
}
