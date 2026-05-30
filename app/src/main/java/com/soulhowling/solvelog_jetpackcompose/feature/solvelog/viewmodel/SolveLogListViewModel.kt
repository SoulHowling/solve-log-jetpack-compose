package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.FolderRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.SolveLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class SolveLogListItem {
    data class FolderItem(val folder: Folder) : SolveLogListItem()
    data class LogItem(val log: SolveLog) : SolveLogListItem()
}

class SolveLogListViewModel(application: Application) : AndroidViewModel(application) {

    private val folderRepository: FolderRepository
    private val solveLogRepository: SolveLogRepository

    private val _currentFolderId = MutableStateFlow<Int?>(null)

    init {
        val database = (application as SolveLogApplication).database
        folderRepository = FolderRepository(database.folderDao())
        solveLogRepository = SolveLogRepository(database.solveLogDao())
    }

    private val allFolders = folderRepository.allFolders
    private val allSolveLogs = solveLogRepository.allSolveLogs

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
        var folder = folders.find { it.id == currentId }
        while (folder != null) {
            path.add(0, folder.name)
            folder = folders.find { it.id == folder.parentFolderId }
        }
        path.joinToString(" / ")
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), null)

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
            val solveLog = solveLogRepository.createSolveLog(
                name = name,
                questionQuantity = questionQuantity,
                selectionType = selectionType,
                selectionQuantity = selectionQuantity,
                folderId = _currentFolderId.value
            )
            solveLogRepository.insertSolveLog(solveLog)
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
                while (currentParentId != null) {
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
}
