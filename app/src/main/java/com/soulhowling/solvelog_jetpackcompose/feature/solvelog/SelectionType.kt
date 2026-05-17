package com.soulhowling.solvelog_jetpackcompose.feature.solvelog

enum class SelectionType(val index: Int, val displayString: String) {
    NUMBER(0,"123"),
    ALPHABET(1,"ABC"),
    KATAKANA(2,"アイウ"),
    CORRECTNESS(3,"◯ ×"),
    ;

}