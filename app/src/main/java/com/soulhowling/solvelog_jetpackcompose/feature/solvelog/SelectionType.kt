package com.soulhowling.solvelog_jetpackcompose.feature.solvelog

enum class SelectionType(val index: Int, val displayString: String) {
    NUMBER(0, "123"),
    ALPHABET(1, "ABC"),
    KATAKANA(2, "アイウ"),
    CORRECTNESS(3, "◯ ×"),
    ;

    private val kanaList: List<String> = listOf(
        "ア", "イ", "ウ", "エ", "オ"
    )

    fun getAnswerString(selectedAnswer: Int): String {
        return when (this) {
            NUMBER -> (selectedAnswer + 1).toString()
            ALPHABET -> ('A' + selectedAnswer).toString()
            KATAKANA -> kanaList.getOrElse(selectedAnswer) { "" }
            CORRECTNESS -> if (selectedAnswer == 0) "◯" else "×"
        }
    }
}