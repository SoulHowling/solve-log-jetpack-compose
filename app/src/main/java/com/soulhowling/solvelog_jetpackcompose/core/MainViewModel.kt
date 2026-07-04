package com.soulhowling.solvelog_jetpackcompose.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import com.soulhowling.solvelog_jetpackcompose.data.PreferenceRepository
import com.soulhowling.solvelog_jetpackcompose.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application): AndroidViewModel(application), AppBarController {
    val themeMode: StateFlow<ThemeMode> = PreferenceRepository.themeMode

    private val _customTitle = MutableStateFlow<String?>(null)
    private val _defaultTitle = MutableStateFlow<String?>(null)
    val title: StateFlow<String?> = combine(_customTitle, _defaultTitle) { custom, default ->
        custom ?: default
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _appBarActions = MutableStateFlow<(@Composable () -> Unit)?>(null)
    val appBarActions: StateFlow<(@Composable () -> Unit)?> = _appBarActions.asStateFlow()

    private val _isBottomBarVisible = MutableStateFlow(true)
    val isBottomBarVisible: StateFlow<Boolean> = _isBottomBarVisible.asStateFlow()

    override fun setTitle(title: String?) {
        _customTitle.value = title
    }

    override fun setActions(actions: @Composable (() -> Unit)?) {
        _appBarActions.value = actions
    }

    override fun setBottomBarVisible(visible: Boolean) {
        _isBottomBarVisible.value = visible
    }

    fun setDefaultTitle(resId: Int?) {
        _defaultTitle.value = resId?.let { application.getString(it) }
    }

    fun loadThemeMode() {
        PreferenceRepository.loadThemeMode(application)
    }
}