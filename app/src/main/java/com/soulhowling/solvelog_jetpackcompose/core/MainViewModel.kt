package com.soulhowling.solvelog_jetpackcompose.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.compose.runtime.Composable
import com.soulhowling.solvelog_jetpackcompose.data.PreferenceRepository
import com.soulhowling.solvelog_jetpackcompose.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(application: Application): AndroidViewModel(application) {
    val themeMode: StateFlow<ThemeMode> = PreferenceRepository.themeMode

    private val _customTitle = MutableStateFlow<String?>(null)
    val customTitle: StateFlow<String?> = _customTitle.asStateFlow()

    private val _appBarActions = MutableStateFlow<(@Composable () -> Unit)?>(null)
    val appBarActions: StateFlow<(@Composable () -> Unit)?> = _appBarActions.asStateFlow()

    fun setCustomAppBar(title: String?, actions: (@Composable () -> Unit)? = null) {
        _customTitle.value = title
        _appBarActions.value = actions
    }

    fun loadThemeMode() {
        PreferenceRepository.loadThemeMode(application)
    }
}