package com.soulhowling.solvelog_jetpackcompose.feature.solvelog

enum class SelectionQuantity(val index: Int, val displayString: String) {
    TWO(0,"2"),
    THREE(1, "3"),
    FOUR(2, "4"),
    FIVE(3, "5"),
    ;

    fun toInt(): Int {
        return displayString.toInt()
    }

    companion object {
        fun from(index: Int): SelectionQuantity {
            return entries.firstOrNull { it.index == index } ?: TWO
        }
    }
}