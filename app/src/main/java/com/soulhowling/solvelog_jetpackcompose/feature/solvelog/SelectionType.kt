package com.soulhowling.solvelog_jetpackcompose.feature.solvelog

import androidx.annotation.StringRes
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils

enum class SelectionType(val index: Int, val displayString: String) {
    NUMBER(0, "123"),
    ALPHABET(1, "ABC"),
    KATAKANA(2, "アイウ"),
    CORRECTNESS(3, "◯ ×"),
    ;

    fun getAnswerString(selectedAnswer: Int): String? {
        if (selectedAnswer == -1) return null
        return when (this) {
            NUMBER -> (selectedAnswer + 1).toString()
            ALPHABET -> ('A' + selectedAnswer).toString()
            KATAKANA -> kanaList.getOrElse(selectedAnswer) { "" }
            CORRECTNESS -> if (selectedAnswer == 0) "◯" else "×"
        }
    }

    companion object {
        @StringRes
        val unselectedResId = R.string.log_viewer_unselected_answer

        private val kanaList: List<String> = listOf(
            "ア", "イ", "ウ", "エ", "オ"
        )

        fun getSelectionTypeStringList(index: Int, selectionQuantity: SelectionQuantity): List<String> {
            return when (index) {
                NUMBER.index -> {
                    (1..selectionQuantity.toInt()).map { it.toString() }
                }
                ALPHABET.index -> {
                    ('A' until 'A' + selectionQuantity.toInt()).map { it.toString() }
                }
                KATAKANA.index -> {
                    kanaList.take(selectionQuantity.toInt())
                }
                CORRECTNESS.index -> {
                    listOf("◯", "×")
                }
                else -> {
                    LogUtils.w("Unexpected index: $index")
                    emptyList()
                }
            }
        }

        fun from(index: Int): SelectionType {
            return entries.find { it.index == index } ?: NUMBER
        }
    }
}