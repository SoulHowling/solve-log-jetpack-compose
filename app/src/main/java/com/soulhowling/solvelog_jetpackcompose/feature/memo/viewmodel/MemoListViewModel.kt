package com.soulhowling.solvelog_jetpackcompose.feature.memo.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.core.database.DBKeys
import com.soulhowling.solvelog_jetpackcompose.data.PreferenceRepository
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.Memo
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.MemoRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.FolderType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.ViewType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.FolderRepository
import com.soulhowling.solvelog_jetpackcompose.utils.FileUtils
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.Serializable

sealed interface MemoListItem {
    data class FolderItem(val folder: Folder) : MemoListItem
    data class MemoItem(val memo: Memo) : MemoListItem
}

sealed class MemoExportTarget : Serializable {
    data class Single(val id: Int, val name: String) : MemoExportTarget()
    data class FolderTarget(val id: Int, val name: String) : MemoExportTarget()
    data object All : MemoExportTarget() {
        private fun readResolve(): Any = All
    }

    val fileName: String
        get() = when (this) {
            is Single -> "${name.ifBlank { "memo" }}.json"
            is FolderTarget -> "${name.ifBlank { "folder" }}.json"
            is All -> "all_memos.json"
        }
}

class MemoListViewModel(application: Application) : AndroidViewModel(application) {
    private val database = (application as SolveLogApplication).database
    private val memoRepository = MemoRepository(database.memoDao())
    private val folderRepository = FolderRepository(database.folderDao())

    private val _currentFolderId = MutableStateFlow<Int?>(null)
    val currentFolderId: StateFlow<Int?> = _currentFolderId

    private val _manualOrder = MutableStateFlow<List<MemoListItem>?>(null)

    val viewType: StateFlow<ViewType> = PreferenceRepository.viewType

    private val allFolders = folderRepository.allFolders.map { list ->
        list.filter { it.folderType == FolderType.MEMO.index }
    }
    private val allMemos = memoRepository.getAllMemos()

    val folders: StateFlow<List<Folder>> = allFolders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val dbSortedItems: StateFlow<List<MemoListItem>> = combine(
        allFolders,
        allMemos,
        _currentFolderId
    ) { folders, memos, currentId ->
        val folderItems = folders
            .filter { it.parentFolderId == currentId }
            .map { MemoListItem.FolderItem(it) }
        val memoItems = memos
            .filter { it.folderId == currentId }
            .map { MemoListItem.MemoItem(it) }

        (folderItems + memoItems).sortedBy { item ->
            when (item) {
                is MemoListItem.FolderItem -> item.folder.priority
                is MemoListItem.MemoItem -> item.memo.priority
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val displayItems: StateFlow<List<MemoListItem>> = combine(
        dbSortedItems,
        _manualOrder
    ) { dbItems, manual ->
        manual ?: dbItems
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _exportTarget = MutableSharedFlow<MemoExportTarget>()
    val exportTarget: SharedFlow<MemoExportTarget> = _exportTarget.asSharedFlow()

    private fun getItemId(item: MemoListItem): String = when (item) {
        is MemoListItem.FolderItem -> "f_${item.folder.id}"
        is MemoListItem.MemoItem -> "m_${item.memo.id}"
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

    fun selectFolder(folderId: Int?) {
        _currentFolderId.value = folderId
    }

    fun navigateBack() {
        val currentId = _currentFolderId.value ?: return
        viewModelScope.launch {
            val foldersList = allFolders.first()
            val currentFolder = foldersList.find { it.id == currentId }
            _currentFolderId.value = currentFolder?.parentFolderId
        }
    }

    fun toggleViewMode() {
        val nextType = ViewType.toggle(viewType.value)
        PreferenceRepository.setViewType(getApplication(), nextType)
    }

    private fun getNextPriority(): Int {
        val currentItems = displayItems.value
        if (currentItems.isEmpty()) return 0
        return currentItems.maxOf { item ->
            when (item) {
                is MemoListItem.FolderItem -> item.folder.priority
                is MemoListItem.MemoItem -> item.memo.priority
            }
        } + 1
    }

    fun insertFolder(name: String) {
        viewModelScope.launch {
            val folder = folderRepository.createFolder(
                name = name,
                parentFolderId = _currentFolderId.value
            ).copy(priority = getNextPriority(), folderType = FolderType.MEMO.index)
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

                val allMemosList = allMemos.first()
                allMemosList.filter { it.folderId in descendantFolderIds }.forEach { memo ->
                    memoRepository.deleteMemo(memo)
                }

                folderRepository.deleteFolder(folder)
            }
        }
    }

    fun moveFolder(folder: Folder, targetFolderId: Int?) {
        viewModelScope.launch {
            if (folder.id == targetFolderId) return@launch

            if (targetFolderId != null) {
                var currentParentId: Int? = targetFolderId
                val foldersList = allFolders.first()
                val visited = mutableSetOf<Int>()
                while (currentParentId != null && visited.add(currentParentId)) {
                    if (currentParentId == folder.id) return@launch
                    currentParentId = foldersList.find { it.id == currentParentId }?.parentFolderId
                }
            }

            val updatedFolder = folder.copy(
                parentFolderId = targetFolderId,
                updatedAt = System.currentTimeMillis()
            )
            folderRepository.upsertFolder(updatedFolder)
        }
    }

    fun moveMemo(memo: Memo, targetFolderId: Int?) {
        viewModelScope.launch {
            val updatedMemo = memo.copy(
                folderId = targetFolderId,
                updatedAt = System.currentTimeMillis()
            )
            memoRepository.upsertMemo(updatedMemo)
        }
    }

    fun deleteMemo(memo: Memo) {
        viewModelScope.launch {
            memoRepository.deleteMemo(memo)
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
                        is MemoListItem.FolderItem -> {
                            if (listItem.folder.priority != index) {
                                folderRepository.upsertFolder(listItem.folder.copy(priority = index))
                            }
                        }
                        is MemoListItem.MemoItem -> {
                            if (listItem.memo.priority != index) {
                                memoRepository.upsertMemo(listItem.memo.copy(priority = index))
                            }
                        }
                    }
                }
            }
        }
    }

    fun exportMemo(memo: Memo) {
        viewModelScope.launch {
            _exportTarget.emit(MemoExportTarget.Single(memo.id, memo.name))
        }
    }

    fun exportFolder(folder: Folder) {
        viewModelScope.launch {
            _exportTarget.emit(MemoExportTarget.FolderTarget(folder.id, folder.name))
        }
    }

    fun exportAllMemos() {
        viewModelScope.launch {
            _exportTarget.emit(MemoExportTarget.All)
        }
    }

    fun performExport(uri: Uri, target: MemoExportTarget, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val jsonString = when (target) {
                        is MemoExportTarget.Single -> {
                            val memo = memoRepository.getMemo(target.id).firstOrNull()
                                ?: return@withContext false
                            FileUtils.generateMemoJson(memo)
                        }
                        is MemoExportTarget.FolderTarget -> {
                            val allFoldersList = allFolders.first()
                            val folder = allFoldersList.find { it.id == target.id }
                                ?: return@withContext false
                            val allMemosList = allMemos.first()
                            FileUtils.generateMemoFolderJson(folder, allFoldersList, allMemosList)
                        }
                        is MemoExportTarget.All -> {
                            val allFoldersList = allFolders.first()
                            val allMemosList = allMemos.firstOrNull() ?: emptyList()
                            FileUtils.generateAllMemosDataJson(allFoldersList, allMemosList)
                        }
                    }
                    FileUtils.writeTextToUri(getApplication(), uri, jsonString)
                } catch (e: Exception) {
                    LogUtils.e("Memo export failed", e)
                    false
                }
            }
            onResult(success)
        }
    }

    fun performImport(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val jsonString = FileUtils.readTextFromUri(getApplication(), uri)
                        ?: return@withContext false
                    val jsonObject = JSONObject(jsonString)

                    database.withTransaction {
                        when {
                            jsonObject.has(FileUtils.KEY_FOLDERS) -> {
                                importAllMemoData(jsonObject, _currentFolderId.value)
                            }
                            jsonObject.has(FileUtils.KEY_SUBFOLDERS) || jsonObject.has(FileUtils.KEY_MEMOS) -> {
                                importMemoFolderOrMemos(jsonObject, _currentFolderId.value)
                            }
                            else -> {
                                val memosToImport = FileUtils.parseMemosJson(jsonString)
                                if (memosToImport.isEmpty()) return@withTransaction
                                val targetFolderId = _currentFolderId.value
                                memosToImport.forEach { memo ->
                                    memoRepository.upsertMemo(memo.copy(id = 0, folderId = targetFolderId))
                                }
                            }
                        }
                    }
                    true
                } catch (e: Exception) {
                    LogUtils.e("Memo import failed", e)
                    false
                }
            }
            onResult(success)
        }
    }

    private suspend fun importAllMemoData(jsonObject: JSONObject, currentFolderId: Int?) {
        val oldFolderIdToNewFolderId = mutableMapOf<Int, Int>()

        if (jsonObject.has(FileUtils.KEY_FOLDERS)) {
            val foldersArray = jsonObject.getJSONArray(FileUtils.KEY_FOLDERS)
            val foldersToInsert = mutableListOf<JSONObject>()
            for (i in 0 until foldersArray.length()) {
                foldersToInsert.add(foldersArray.getJSONObject(i))
            }

            val remainingFolders = foldersToInsert.toMutableList()
            var prevSize = -1
            while (remainingFolders.isNotEmpty() && remainingFolders.size != prevSize) {
                prevSize = remainingFolders.size
                val iterator = remainingFolders.iterator()
                while (iterator.hasNext()) {
                    val folderJson = iterator.next()
                    val oldId = folderJson.getInt(DBKeys.KEY_ID)
                    val name = folderJson.getString(DBKeys.KEY_NAME)
                    val oldParentId = if (folderJson.has(DBKeys.KEY_PARENT_FOLDER_ID) && !folderJson.isNull(DBKeys.KEY_PARENT_FOLDER_ID)) {
                        folderJson.getInt(DBKeys.KEY_PARENT_FOLDER_ID)
                    } else null

                    if (oldParentId == null) {
                        val newFolder = folderRepository.createFolder(
                            name = name,
                            parentFolderId = currentFolderId
                        ).copy(folderType = FolderType.MEMO.index)
                        val newId = folderRepository.insertAndReturnId(newFolder)
                        oldFolderIdToNewFolderId[oldId] = newId.toInt()
                        iterator.remove()
                    } else if (oldFolderIdToNewFolderId.containsKey(oldParentId)) {
                        val newParentId = oldFolderIdToNewFolderId[oldParentId]
                        val newFolder = folderRepository.createFolder(
                            name = name,
                            parentFolderId = newParentId
                        ).copy(folderType = FolderType.MEMO.index)
                        val newId = folderRepository.insertAndReturnId(newFolder)
                        oldFolderIdToNewFolderId[oldId] = newId.toInt()
                        iterator.remove()
                    }
                }
            }
        }

        if (jsonObject.has(FileUtils.KEY_MEMOS)) {
            val memosArray = jsonObject.getJSONArray(FileUtils.KEY_MEMOS)
            for (i in 0 until memosArray.length()) {
                val memoJson = memosArray.getJSONObject(i)
                val memo = FileUtils.parseMemoJson(memoJson)
                val targetFolderId = if (memo.folderId != null && oldFolderIdToNewFolderId.containsKey(memo.folderId)) {
                    oldFolderIdToNewFolderId[memo.folderId]
                } else if (memo.folderId == null) {
                    currentFolderId
                } else null

                memoRepository.upsertMemo(memo.copy(id = 0, folderId = targetFolderId))
            }
        }
    }

    private suspend fun importMemoFolderOrMemos(folderJson: JSONObject, parentFolderId: Int?) {
        val oldFolderIdToNewFolderId = mutableMapOf<Int, Int>()

        suspend fun importSingleFolder(json: JSONObject, targetParentId: Int?): Int {
            val name = json.getString(DBKeys.KEY_NAME)
            val oldId = json.optInt(DBKeys.KEY_ID, -1)
            val newFolder = folderRepository.createFolder(
                name = name,
                parentFolderId = targetParentId
            ).copy(folderType = FolderType.MEMO.index)
            val newId = folderRepository.insertAndReturnId(newFolder).toInt()
            if (oldId != -1) {
                oldFolderIdToNewFolderId[oldId] = newId
            }

            if (json.has(FileUtils.KEY_MEMOS)) {
                val memosArray = json.getJSONArray(FileUtils.KEY_MEMOS)
                for (i in 0 until memosArray.length()) {
                    val memoJson = memosArray.getJSONObject(i)
                    val memo = FileUtils.parseMemoJson(memoJson)
                    memoRepository.upsertMemo(memo.copy(id = 0, folderId = newId))
                }
            }

            if (json.has(FileUtils.KEY_SUBFOLDERS)) {
                val subfoldersArray = json.getJSONArray(FileUtils.KEY_SUBFOLDERS)
                for (i in 0 until subfoldersArray.length()) {
                    importSingleFolder(subfoldersArray.getJSONObject(i), newId)
                }
            }
            return newId
        }

        if (folderJson.has(DBKeys.KEY_NAME)) {
            importSingleFolder(folderJson, parentFolderId)
        } else if (folderJson.has(FileUtils.KEY_MEMOS)) {
            val memosArray = folderJson.getJSONArray(FileUtils.KEY_MEMOS)
            for (i in 0 until memosArray.length()) {
                val memo = FileUtils.parseMemoJson(memosArray.getJSONObject(i))
                memoRepository.upsertMemo(memo.copy(id = 0, folderId = parentFolderId))
            }
        }
    }
}
