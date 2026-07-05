package com.soulhowling.solvelog_jetpackcompose.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

interface AppBarController {
    fun setTitle(title: String?)
    fun setActions(actions: (@Composable () -> Unit)?)
    fun setBottomBarVisible(visible: Boolean)
}

val LocalAppBarController = staticCompositionLocalOf<AppBarController> {
    object : AppBarController {
        override fun setTitle(title: String?) {}
        override fun setActions(actions: (@Composable () -> Unit)?) {}
        override fun setBottomBarVisible(visible: Boolean) {}
    }
}