package com.soulhowling.solvelog_jetpackcompose.feature.memo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.Memo
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.MemoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class MemoViewViewModel(application: Application) : AndroidViewModel(application) {
    private val database = (application as SolveLogApplication).database
    private val memoRepository = MemoRepository(database.memoDao())

    private val _memoId = MutableStateFlow(-1)

    val memo: StateFlow<Memo?> = _memoId
        .flatMapLatest { id ->
            if (id == -1) flowOf(null) else memoRepository.getMemo(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun loadMemo(id: Int) {
        _memoId.value = id
    }
}
