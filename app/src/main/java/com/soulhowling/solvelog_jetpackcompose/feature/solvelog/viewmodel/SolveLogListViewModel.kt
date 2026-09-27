package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.AnswerFormat
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.Correctness
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.FolderType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.FolderRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.QuestionRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.SolveLogRepository
import com.soulhowling.solvelog_jetpackcompose.utils.FileUtils
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils
import com.soulhowling.solvelog_jetpackcompose.core.database.DBKeys
import com.soulhowling.solvelog_jetpackcompose.data.PreferenceRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.ViewType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

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
    private val _manualOrder = MutableStateFlow<List<SolveLogListItem>?>(null)

    private val _exportTarget = MutableSharedFlow<ExportTarget>()
    val exportTarget = _exportTarget.asSharedFlow()

    val viewType: StateFlow<ViewType> = PreferenceRepository.viewType

    private val allFolders = folderRepository.allFolders.map { list ->
        list.filter { it.folderType == FolderType.SOLVE_LOG.index }
    }
    private val allSolveLogs = solveLogRepository.allSolveLogs

    val folders: StateFlow<List<Folder>> = allFolders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val dbSortedItems: StateFlow<List<SolveLogListItem>> = combine(
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

        (folderItems + logItems).sortedBy { item ->
            when (item) {
                is SolveLogListItem.FolderItem -> item.folder.priority
                is SolveLogListItem.LogItem -> item.log.priority
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val displayItems: StateFlow<List<SolveLogListItem>> = combine(
        dbSortedItems,
        _manualOrder
    ) { dbItems, manual ->
        manual ?: dbItems
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun getItemId(item: SolveLogListItem): String = when (item) {
        is SolveLogListItem.FolderItem -> "f_${item.folder.id}"
        is SolveLogListItem.LogItem -> "l_${item.log.id}"
    }

    init {
        PreferenceRepository.loadViewType(application)
        viewModelScope.launch {
            dbSortedItems.collect { dbItems ->
                val manual = _manualOrder.value ?: return@collect
                val isSynced = dbItems.size == manual.size &&
                        dbItems.zip(manual).all { (a, b) -> getItemId(a) == getItemId(b) }
                if (isSynced) {
                    _manualOrder.value = null
                }
            }
        }
    }

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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectFolder(folderId: Int?) {
        _currentFolderId.value = folderId
    }

    fun toggleViewMode() {
        val viewType = ViewType.toggle(viewType.value)
        PreferenceRepository.setViewType(getApplication(), viewType)
    }

    fun navigateBack() {
        val currentId = _currentFolderId.value ?: return
        viewModelScope.launch {
            val currentFolder = folders.first().find { it.id == currentId }
            _currentFolderId.value = currentFolder?.parentFolderId
        }
    }

    private fun getNextPriority(): Int {
        val currentItems = displayItems.value
        if (currentItems.isEmpty()) return 0
        return currentItems.maxOf { item ->
            when (item) {
                is SolveLogListItem.FolderItem -> item.folder.priority
                is SolveLogListItem.LogItem -> item.log.priority
            }
        } + 1
    }

    fun insertSolveLog(
        name: String,
        questionQuantity: Int,
        answerFormat: AnswerFormat,
        selectionType: SelectionType,
        selectionQuantity: SelectionQuantity
    ) {
        viewModelScope.launch {
            database.withTransaction {
                val solveLog = solveLogRepository.createSolveLog(
                    name = name,
                    questionQuantity = questionQuantity,
                    answerFormat = answerFormat,
                    selectionType = selectionType,
                    selectionQuantity = selectionQuantity,
                    folderId = _currentFolderId.value
                ).copy(priority = getNextPriority())
                val solveLogId = solveLogRepository.insertAndReturnId(solveLog)

                val questions = (0 until questionQuantity).map { index ->
                    questionRepository.createQuestion(
                        attempt = 1,
                        selectedAnswer = -1,
                        questionIndex = index,
                        freeAnswerContent = null,
                        correctness = Correctness.NONE.index,
                        isFlag = false,
                        solveLogId = solveLogId
                    )
                }
                questionRepository.insertQuestions(questions)
            }
        }
    }

    fun updateSolveLog(
        solveLog: SolveLog,
        name: String,
        questionQuantity: Int,
        answerFormat: AnswerFormat,
        selectionType: SelectionType,
        selectionQuantity: SelectionQuantity
    ) {
        viewModelScope.launch {
            database.withTransaction {
                val updatedLog = solveLog.copy(
                    name = name,
                    questionQuantity = questionQuantity,
                    answerFormat = answerFormat.index,
                    selectionType = selectionType.index,
                    selectionQuantity = selectionQuantity.index,
                    updatedAt = System.currentTimeMillis()
                )
                solveLogRepository.upsertSolveLog(updatedLog)

                if (questionQuantity != solveLog.questionQuantity) {
                    val currentQuestions = questionRepository.getQuestionListBy(solveLog.id)
                    if (questionQuantity > solveLog.questionQuantity) {
                        val attempts = currentQuestions.map { it.attempt }.distinct()
                            .ifEmpty { 1..solveLog.attemptCount }
                        val newQuestions = attempts.flatMap { attempt ->
                            (solveLog.questionQuantity until questionQuantity).map { index ->
                                questionRepository.createQuestion(
                                    attempt = attempt,
                                    selectedAnswer = -1,
                                    questionIndex = index,
                                    freeAnswerContent = null,
                                    correctness = Correctness.NONE.index,
                                    isFlag = false,
                                    solveLogId = solveLog.id
                                )
                            }
                        }
                        questionRepository.insertQuestions(newQuestions)
                    } else {
                        val questionsToDelete =
                            currentQuestions.filter { it.questionIndex >= questionQuantity }
                        questionRepository.deleteQuestions(questionsToDelete)
                    }
                }
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
            ).copy(priority = getNextPriority(), folderType = FolderType.SOLVE_LOG.index)
            folderRepository.insertFolder(folder)
        }
    }

    fun updateFolder(folder: Folder, name: String) {
        viewModelScope.launch {
            val updatedFolder = folder.copy(
                name = name,
                updatedAt = System.currentTimeMillis()
            )
            folderRepository.upsertFolder(updatedFolder)
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            database.withTransaction {
                val allFoldersList = allFolders.first()
                val descendantFolderIds = mutableSetOf<Int>()
                fun collectFolderIds(id: Int) {
                    if (descendantFolderIds.add(id)) {
                        allFoldersList.filter { it.parentFolderId == id }
                            .forEach { collectFolderIds(it.id) }
                    }
                }
                collectFolderIds(folder.id)

                val allLogsList = solveLogRepository.allSolveLogs.first()
                allLogsList.filter { it.folderId in descendantFolderIds }.forEach { log ->
                    solveLogRepository.deleteSolveLog(log)
                }

                folderRepository.deleteFolder(folder)
            }
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

    fun reorder(fromIndex: Int, toIndex: Int) {
        val currentItems = displayItems.value.toMutableList()
        if (fromIndex !in currentItems.indices || toIndex !in currentItems.indices) return

        val item = currentItems.removeAt(fromIndex)
        currentItems.add(toIndex, item)
        
        _manualOrder.value = currentItems

        viewModelScope.launch {
            database.withTransaction {
                currentItems.forEachIndexed { index, listItem ->
                    when (listItem) {
                        is SolveLogListItem.FolderItem -> {
                            if (listItem.folder.priority != index) {
                                folderRepository.upsertFolder(listItem.folder.copy(priority = index))
                            }
                        }
                        is SolveLogListItem.LogItem -> {
                            if (listItem.log.priority != index) {
                                solveLogRepository.upsertSolveLog(listItem.log.copy(priority = index))
                            }
                        }
                    }
                }
            }
        }
    }

    fun performExport(uri: android.net.Uri, target: ExportTarget, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val jsonString = when (target) {
                        is ExportTarget.Log -> {
                            val solveLog = solveLogRepository.getSolveLog(target.id).first()
                                ?: return@withContext false
                            val questions = questionRepository.getQuestionListBy(target.id)
                            FileUtils.generateSolveLogJson(solveLog, questions)
                        }

                        is ExportTarget.Folder -> {
                            val allFolders = folderRepository.allFolders.first()
                            val folder =
                                allFolders.find { it.id == target.id } ?: return@withContext false
                            val allLogs = solveLogRepository.allSolveLogs.first()

                            val descendantFolderIds = mutableSetOf<Int>()
                            fun collectFolderIds(id: Int) {
                                if (descendantFolderIds.add(id)) {
                                    allFolders.filter { it.parentFolderId == id }
                                        .forEach { collectFolderIds(it.id) }
                                }
                            }
                            collectFolderIds(target.id)

                            val logsToExport = allLogs.filter { it.folderId in descendantFolderIds }
                            val logIds = logsToExport.map { it.id }
                            val allQuestions = questionRepository.getQuestionsBySolveLogIds(logIds)
                            val logQuestionsMap = allQuestions.groupBy { it.solveLogId }

                            FileUtils.generateFolderJson(
                                folder,
                                allFolders,
                                allLogs,
                                logQuestionsMap
                            )
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

    fun performImport(uri: android.net.Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val jsonString =
                        FileUtils.readTextFromUri(getApplication(), uri) ?: return@withContext false
                    val jsonObject = JSONObject(jsonString)

                    database.withTransaction {
                        when {
                            jsonObject.has(FileUtils.KEY_FOLDERS) -> {
                                importAllData(jsonObject, _currentFolderId.value)
                            }

                            jsonObject.has(FileUtils.KEY_QUESTIONS) -> {
                                importSolveLog(jsonObject, _currentFolderId.value)
                            }

                            jsonObject.has(FileUtils.KEY_SUBFOLDERS) || jsonObject.has(FileUtils.KEY_SOLVE_LOGS) -> {
                                importFolder(jsonObject, _currentFolderId.value)
                            }

                            else -> false
                        }
                    }
                } catch (e: Exception) {
                    LogUtils.e("Import failed", e)
                    false
                }
            }
            onResult(success)
        }
    }

    private suspend fun importAllData(jsonObject: JSONObject, targetFolderId: Int?): Boolean {
        val foldersArray = jsonObject.getJSONArray(FileUtils.KEY_FOLDERS)
        val logsArray = jsonObject.getJSONArray(FileUtils.KEY_SOLVE_LOGS)

        val oldToNewFolderId = mutableMapOf<Int, Int?>()
        val foldersToInsert = mutableListOf<JSONObject>()
        for (i in 0 until foldersArray.length()) {
            foldersToInsert.add(foldersArray.getJSONObject(i))
        }

        val importFolderIds = foldersToInsert.map { it.getInt(DBKeys.KEY_ID) }.toSet()
        val remainingFolders = foldersToInsert.toMutableList()
        while (remainingFolders.isNotEmpty()) {
            val it = remainingFolders.iterator()
            var insertedAny = false
            while (it.hasNext()) {
                val folderJson = it.next()
                val oldId = folderJson.getInt(DBKeys.KEY_ID)
                val oldParentId =
                    if (folderJson.isNull(DBKeys.KEY_PARENT_FOLDER_ID)) null else folderJson.getInt(
                        DBKeys.KEY_PARENT_FOLDER_ID
                    )

                val isParentAvailable = oldParentId == null
                        || !importFolderIds.contains(oldParentId)
                        || oldToNewFolderId.containsKey(oldParentId)

                if (isParentAvailable) {
                    val newParentId =
                        if (oldParentId == null || !importFolderIds.contains(oldParentId)) {
                            targetFolderId
                        } else {
                            oldToNewFolderId[oldParentId]
                        }
                    val folder = Folder(
                        name = folderJson.getString(DBKeys.KEY_NAME),
                        parentFolderId = newParentId,
                        createdAt = folderJson.optLong(
                            DBKeys.KEY_CREATED_AT,
                            System.currentTimeMillis()
                        ),
                        updatedAt = folderJson.optLong(
                            DBKeys.KEY_UPDATED_AT,
                            System.currentTimeMillis()
                        )
                    )
                    val newId = folderRepository.insertAndReturnId(folder)
                    oldToNewFolderId[oldId] = newId.toInt()
                    it.remove()
                    insertedAny = true
                }
            }
            if (!insertedAny) {
                if (remainingFolders.isNotEmpty()) {
                    throw IllegalArgumentException("Circular folder structure detected in the imported data.")
                }
                break
            }
        }

        for (i in 0 until logsArray.length()) {
            val logJson = logsArray.getJSONObject(i)
            val oldFolderId =
                if (logJson.isNull(DBKeys.KEY_FOLDER_ID)) null else logJson.getInt(DBKeys.KEY_FOLDER_ID)
            val newFolderId =
                if (oldFolderId == null) targetFolderId else oldToNewFolderId[oldFolderId]
                    ?: targetFolderId
            importSolveLog(logJson, newFolderId)
        }
        return true
    }

    private suspend fun importFolder(folderJson: JSONObject, parentFolderId: Int?): Boolean {
        val folder = Folder(
            name = folderJson.getString(DBKeys.KEY_NAME),
            parentFolderId = parentFolderId,
            createdAt = folderJson.optLong(DBKeys.KEY_CREATED_AT, System.currentTimeMillis()),
            updatedAt = folderJson.optLong(DBKeys.KEY_UPDATED_AT, System.currentTimeMillis())
        )
        val newFolderId = folderRepository.insertAndReturnId(folder).toInt()

        if (folderJson.has(FileUtils.KEY_SOLVE_LOGS)) {
            val logsArray = folderJson.getJSONArray(FileUtils.KEY_SOLVE_LOGS)
            for (i in 0 until logsArray.length()) {
                importSolveLog(logsArray.getJSONObject(i), newFolderId)
            }
        }

        if (folderJson.has(FileUtils.KEY_SUBFOLDERS)) {
            val subfoldersArray = folderJson.getJSONArray(FileUtils.KEY_SUBFOLDERS)
            for (i in 0 until subfoldersArray.length()) {
                importFolder(subfoldersArray.getJSONObject(i), newFolderId)
            }
        }
        return true
    }

    private suspend fun importSolveLog(logJson: JSONObject, folderId: Int?): Boolean {
        val solveLog = SolveLog(
            name = logJson.getString(DBKeys.KEY_NAME),
            questionQuantity = logJson.getInt(DBKeys.KEY_QUESTION_QUANTITY),
            answerFormat = logJson.optInt(DBKeys.KEY_ANSWER_FORMAT, AnswerFormat.MULTIPLE_CHOICE.index),
            selectionType = logJson.getInt(DBKeys.KEY_SELECTION_TYPE),
            selectionQuantity = logJson.getInt(DBKeys.KEY_SELECTION_QUANTITY),
            attemptCount = logJson.optInt(DBKeys.KEY_ATTEMPT_COUNT, 1),
            createdAt = logJson.optLong(DBKeys.KEY_CREATED_AT, System.currentTimeMillis()),
            updatedAt = logJson.optLong(DBKeys.KEY_UPDATED_AT, System.currentTimeMillis()),
            folderId = folderId
        )
        val solveLogId = solveLogRepository.insertAndReturnId(solveLog)

        if (logJson.has(FileUtils.KEY_QUESTIONS)) {
            val questionsArray = logJson.getJSONArray(FileUtils.KEY_QUESTIONS)
            val questions = mutableListOf<Question>()
            for (i in 0 until questionsArray.length()) {
                val qJson = questionsArray.getJSONObject(i)
                questions.add(
                    Question(
                        attempt = qJson.getInt(DBKeys.KEY_ATTEMPT),
                        questionIndex = qJson.getInt(DBKeys.KEY_QUESTION_INDEX),
                        freeAnswerContent = if (qJson.isNull(DBKeys.KEY_FREE_ANSWER_CONTENT)) null else qJson.getString(DBKeys.KEY_FREE_ANSWER_CONTENT),
                        selectedAnswer = qJson.getInt(DBKeys.KEY_SELECTED_ANSWER),
                        correctness = qJson.getInt(DBKeys.KEY_CORRECTNESS),
                        isFlag = qJson.getBoolean(DBKeys.KEY_IS_FLAG),
                        solveLogId = solveLogId
                    )
                )
            }
            questionRepository.insertQuestions(questions)
        }
        return true
    }
}
