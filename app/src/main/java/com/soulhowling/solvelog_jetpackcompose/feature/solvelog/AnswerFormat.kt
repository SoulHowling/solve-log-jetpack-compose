package com.soulhowling.solvelog_jetpackcompose.feature.solvelog

import androidx.annotation.StringRes
import com.soulhowling.solvelog_jetpackcompose.R

enum class AnswerFormat(val index: Int, @StringRes val displayStringId: Int) {
    MULTIPLE_CHOICE(0, R.string.log_answer_format_multiple_choice),
    FREE_RESPONSE(1, R.string.log_answer_format_free_response)
    ;

    companion object {
        fun from(index: Int): AnswerFormat {
            return entries.find { it.index == index } ?: MULTIPLE_CHOICE
        }
    }

}