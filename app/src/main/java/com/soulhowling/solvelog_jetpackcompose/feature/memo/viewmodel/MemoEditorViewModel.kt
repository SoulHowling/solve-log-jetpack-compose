package com.soulhowling.solvelog_jetpackcompose.feature.memo.viewmodel

import android.app.Application
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.Memo
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.MemoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MemoEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val applicationScope = (application as SolveLogApplication).applicationScope
    private val database = (application as SolveLogApplication).database
    private val memoRepository = MemoRepository(database.memoDao())

    private var memoId: Int = -1
    private var currentFolderId: Int? = null
    private var initialMemo: Memo? = null
    private var isInitialLoading = false

    private val _memoName = MutableStateFlow("")
    val memoName: StateFlow<String> = _memoName.asStateFlow()

    private val _memoContent = MutableStateFlow(TextFieldValue(""))
    val memoContent: StateFlow<TextFieldValue> = _memoContent.asStateFlow()

    private var saveJob: Job? = null

    fun loadMemo(id: Int, targetFolderId: Int? = null) {
        if (id == -1) {
            memoId = -1
            currentFolderId = targetFolderId
            initialMemo = null
            _memoName.value = ""
            _memoContent.value = TextFieldValue("")
            return
        }
        if (memoId == id && initialMemo != null) return

        memoId = id
        isInitialLoading = true
        viewModelScope.launch {
            val memo = memoRepository.getMemo(id).firstOrNull()
            if (memo != null) {
                initialMemo = memo
                currentFolderId = memo.folderId
                _memoName.value = memo.name
                _memoContent.value = TextFieldValue(memo.content)
            }
            isInitialLoading = false
        }
    }

    fun onNameChange(newName: String) {
        if (_memoName.value == newName) return
        _memoName.value = newName
        triggerAutoSave()
    }

    fun onContentChange(newContent: TextFieldValue) {
        val textChanged = _memoContent.value.text != newContent.text
        _memoContent.value = newContent
        if (textChanged) {
            triggerAutoSave()
        }
    }

    fun insertTemplate(template: String) {
        val current = _memoContent.value
        val selection = current.selection
        val text = current.text

        val newText = StringBuilder(text)
            .replace(selection.start, selection.end, template)
            .toString()

        val newCursor = selection.start + template.length

        _memoContent.value = current.copy(
            text = newText,
            selection = TextRange(newCursor)
        )
        triggerAutoSave()
    }

    private fun triggerAutoSave() {
        if (isInitialLoading) return
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(300.milliseconds)
            saveMemoInternal()
        }
    }

    fun forceSave(onSaved: ((Int) -> Unit)? = null) {
        saveJob?.cancel()
        saveMemoInternal(onSaved)
    }

    private fun saveMemoInternal(onSaved: ((Int) -> Unit)? = null) {
        val name = _memoName.value
        val content = _memoContent.value.text

        if (name.isBlank() && content.isBlank()) {
            onSaved?.invoke(memoId)
            return
        }

        if (initialMemo != null && initialMemo?.name == name && initialMemo?.content == content) {
            onSaved?.invoke(memoId)
            return
        }

        applicationScope.launch {
            val now = System.currentTimeMillis()
            val memo = if (memoId == -1) {
                val maxPriority = database.memoDao().getMaxPriority() ?: -1
                memoRepository.createMemo(name, content, folderId = currentFolderId).copy(priority = maxPriority + 1)
            } else {
                initialMemo?.copy(
                    name = name,
                    content = content,
                    updatedAt = now
                ) ?: Memo(
                    id = memoId,
                    name = name,
                    content = content,
                    createdAt = now,
                    updatedAt = now,
                    folderId = currentFolderId
                )
            }

            val savedId = memoRepository.upsertMemo(memo)
            if (memoId == -1) {
                memoId = savedId.toInt()
            }
            initialMemo = memo.copy(id = memoId)
            onSaved?.invoke(memoId)
        }
    }
}
