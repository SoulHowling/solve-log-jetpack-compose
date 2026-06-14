package com.soulhowling.solvelog_jetpackcompose.feature.solvelog

enum class Correctness(val index: Int, val symbol: String) {
    NONE(0, "-"),
    CORRECT(1, "◯"),
    INCORRECT(2, "×"),
    ;
}