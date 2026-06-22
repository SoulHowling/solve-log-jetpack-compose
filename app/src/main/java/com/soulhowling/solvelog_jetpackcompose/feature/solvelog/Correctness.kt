package com.soulhowling.solvelog_jetpackcompose.feature.solvelog

enum class Correctness(val index: Int, val symbol: String) {
    NONE(0, "-"),
    CORRECT(1, "◯"),
    INCORRECT(2, "×"),
    ;

    companion object {
        fun from(index: Int): Correctness {
            return entries.firstOrNull { it.index == index } ?: NONE
        }
    }
}