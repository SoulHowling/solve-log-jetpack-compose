package com.soulhowling.solvelog_jetpackcompose.feature.solvelog

enum class FolderType(val index: Int) {
    SOLVE_LOG(0),
    MEMO(1),
    ;

    companion object {
        fun from(index: Int): FolderType {
            return entries.find { it.index == index } ?: SOLVE_LOG
        }
    }
}
