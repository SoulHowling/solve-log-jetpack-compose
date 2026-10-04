package com.soulhowling.solvelog_jetpackcompose.core.database

object DBKeys {

    // Table name
    const val TABLE_SOLVE_LOG = "solve_log"
    const val TABLE_QUESTION = "question"
    const val TABLE_FOLDER = "folder"
    const val TABLE_TIMER = "timer_state"
    const val TABLE_MEMO = "memo"

    // Common params
    const val KEY_ID = "id"

    // Solve log params
    const val KEY_NAME = "name"
    const val KEY_QUESTION_QUANTITY = "questionQuantity"
    const val KEY_ANSWER_FORMAT = "answerFormat"
    const val KEY_SELECTION_TYPE = "selectionType"
    const val KEY_SELECTION_QUANTITY = "selectionQuantity"
    const val KEY_ATTEMPT_COUNT = "attemptCount"
    const val KEY_CREATED_AT = "createdAt"
    const val KEY_UPDATED_AT = "updatedAt"
    const val KEY_FOLDER_ID = "folderId"
    const val KEY_PRIORITY = "priority"

    // Question params
    const val KEY_ATTEMPT = "attempt"
    const val KEY_QUESTION_INDEX = "questionIndex"
    const val KEY_FREE_ANSWER_CONTENT = "freeAnswerContent"
    const val KEY_SELECTED_ANSWER = "selectedAnswer"
    const val KEY_CORRECTNESS = "correctness"
    const val KEY_IS_FLAG = "isFlag"
    const val KEY_SOLVE_LOG_ID = "solveLogId"

    // Folder params
    const val KEY_PARENT_FOLDER_ID = "parentFolderId"
}