package com.soulhowling.solvelog_jetpackcompose.feature.solvelog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.ui.graphics.vector.ImageVector


enum class ViewType(val index: Int, val nextButtonImage: ImageVector) {
    CARD(0, Icons.AutoMirrored.Filled.ViewList),
    LIST(1, Icons.Default.ViewAgenda),
    ;

    companion object {
        fun from(index: Int): ViewType {
            return entries.find { it.index == index } ?: CARD
        }

        fun toggle(viewType: ViewType): ViewType {
            return when (viewType) {
                CARD -> LIST
                LIST -> CARD
            }
        }
    }
}