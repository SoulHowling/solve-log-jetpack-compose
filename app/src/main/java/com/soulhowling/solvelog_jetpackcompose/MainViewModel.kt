package com.soulhowling.solvelog_jetpackcompose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.soulhowling.solvelog_jetpackcompose.data.PreferenceRepository
import com.soulhowling.solvelog_jetpackcompose.ui.theme.ThemeMode
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(application: Application): AndroidViewModel(application) {
    val themeMode: StateFlow<ThemeMode> = PreferenceRepository.themeMode

    fun loadThemeMode() {
        PreferenceRepository.loadThemeMode(application)
    }
}