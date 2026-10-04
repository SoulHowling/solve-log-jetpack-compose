package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.AnswerFormat
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import com.soulhowling.solvelog_jetpackcompose.utils.compose.SingleChoiceSegmentedButton

object SolveLogAddBottomSheetTestTags {
    const val TITLE_FIELD = "solve_log_add_title_field"
    const val QUANTITY_FIELD = "solve_log_add_quantity_field"
    const val CONFIRM_BUTTON = "solve_log_add_confirm_button"
    const val TYPE_SELECTOR = "solve_log_add_type_selector"
    const val ANSWER_FORMAT_SELECTOR = "solve_log_add_answer_format_selector"
    const val SELECTION_QUANTITY_SELECTOR = "solve_log_add_selection_quantity_selector"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolveLogAddBottomSheet(
    initialName: String = "",
    initialQuestionQuantity: Int? = null,
    initialAnswerFormat: AnswerFormat = AnswerFormat.MULTIPLE_CHOICE,
    initialSelectionType: SelectionType = SelectionType.NUMBER,
    initialSelectionQuantity: SelectionQuantity = SelectionQuantity.FOUR,
    titleResId: Int = R.string.log_list_add_new_log_title,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, AnswerFormat, SelectionType, SelectionQuantity) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
    ) {
        SolveLogAddContent(
            initialName = initialName,
            initialQuestionQuantity = initialQuestionQuantity,
            initialAnswerFormat = initialAnswerFormat,
            initialSelectionType = initialSelectionType,
            initialSelectionQuantity = initialSelectionQuantity,
            titleResId = titleResId,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
internal fun SolveLogAddContent(
    initialName: String = "",
    initialQuestionQuantity: Int? = null,
    initialAnswerFormat: AnswerFormat = AnswerFormat.MULTIPLE_CHOICE,
    initialSelectionType: SelectionType = SelectionType.NUMBER,
    initialSelectionQuantity: SelectionQuantity = SelectionQuantity.FOUR,
    titleResId: Int = R.string.log_list_add_new_log_title,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, AnswerFormat, SelectionType, SelectionQuantity) -> Unit
) {
    val listState = rememberLazyListState()
    val itemKindList = SelectionType.entries.map { it.displayString }
    val itemQuantityList = SelectionQuantity.entries.map { it.displayString }

    val answerFormatList = AnswerFormat.entries.map { stringResource(it.displayStringId) }
    var selectedAnswerFormat by rememberSaveable(initialAnswerFormat) { mutableStateOf(initialAnswerFormat) }

    var selectedKind by rememberSaveable(initialSelectionType) { mutableStateOf(initialSelectionType) }
    var selectedQuantity by rememberSaveable(initialSelectionQuantity) { mutableStateOf(initialSelectionQuantity) }

    val isSelectionTypeEnabled = selectedAnswerFormat == AnswerFormat.MULTIPLE_CHOICE
    val isSelectionQuantityEnabled = selectedKind != SelectionType.CORRECTNESS

    val sectionSpace = Dimensions.SPACING_MEDIUM

    var logTitle by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var questionQuantity by rememberSaveable(initialQuestionQuantity) {
        mutableStateOf(
            initialQuestionQuantity?.toString() ?: ""
        )
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp)
            .padding(horizontal = Dimensions.PADDING_MEDIUM)
    ) {
        item {
            Text(
                text = stringResource(titleResId),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimensions.PADDING_MEDIUM),
                textAlign = TextAlign.Center
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.log_list_label_log_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextField(
                    value = logTitle,
                    onValueChange = {
                        logTitle = it
                    },
                    placeholder = { Text(stringResource(R.string.log_list_placeholder_log_title)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SolveLogAddBottomSheetTestTags.TITLE_FIELD)
                )
                Spacer(modifier = Modifier.height(sectionSpace))

                Text(
                    text = stringResource(R.string.log_list_label_question_quantity),
                    style = MaterialTheme.typography.titleMedium
                )
                TextField(
                    value = questionQuantity,
                    onValueChange = {
                        questionQuantity = it
                    },
                    placeholder = { Text(stringResource(R.string.log_list_placeholder_question_quantity)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SolveLogAddBottomSheetTestTags.QUANTITY_FIELD),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(sectionSpace))

                Text(
                    text = stringResource(R.string.log_list_label_answer_format),
                    style = MaterialTheme.typography.titleMedium
                )

                SingleChoiceSegmentedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SolveLogAddBottomSheetTestTags.ANSWER_FORMAT_SELECTOR),
                    itemList = answerFormatList,
                    selectedIndex = selectedAnswerFormat.index,
                    onSelectedIndexChange = { selectedAnswerFormat = AnswerFormat.entries[it] },
                    isEnabled = true
                )

                Spacer(modifier = Modifier.height(sectionSpace))


                Text(
                    text = stringResource(R.string.log_list_label_selection_type),
                    style = MaterialTheme.typography.titleMedium
                )
                SingleChoiceSegmentedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SolveLogAddBottomSheetTestTags.TYPE_SELECTOR),
                    itemList = itemKindList,
                    selectedIndex = selectedKind.index,
                    onSelectedIndexChange = { selectedKind = SelectionType.entries[it] },
                    isEnabled = isSelectionTypeEnabled
                )
                Spacer(modifier = Modifier.height(sectionSpace))


                Text(
                    text = stringResource(R.string.log_list_label_selection_quantity),
                    style = MaterialTheme.typography.titleMedium
                )
                SingleChoiceSegmentedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SolveLogAddBottomSheetTestTags.SELECTION_QUANTITY_SELECTOR),
                    itemList = itemQuantityList,
                    selectedIndex = if (isSelectionQuantityEnabled) selectedQuantity.index else SelectionQuantity.TWO.index,
                    onSelectedIndexChange = { selectedQuantity = SelectionQuantity.entries[it] },
                    isEnabled = isSelectionTypeEnabled && isSelectionQuantityEnabled
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))
            Button(
                onClick = {
                    onConfirm(
                        logTitle.trim(),
                        questionQuantity.toIntOrZero(),
                        selectedAnswerFormat,
                        selectedKind,
                        if (isSelectionQuantityEnabled) selectedQuantity else SelectionQuantity.TWO
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(SolveLogAddBottomSheetTestTags.CONFIRM_BUTTON),
                enabled = isAddButtonEnabled(
                    logTitle = logTitle,
                    questionQuantity = questionQuantity
                )
            ) {
                Text(
                    stringResource(
                        if (initialName.isEmpty()) {
                            R.string.common_dialog_add_button
                        } else {
                            R.string.common_dialog_update_button
                        }
                    )
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(Dimensions.SPACING_LARGE))
        }
    }
}

private fun isAddButtonEnabled(logTitle: String, questionQuantity: String): Boolean {
    return logTitle.isNotBlank() && questionQuantity.isNotBlank() && questionQuantity.toIntOrZero() in 1..1000
}

private fun String.toIntOrZero(): Int {
    return toIntOrNull() ?: 0
}

@Preview(showBackground = true)
@Composable
private fun SolveLogAddContentPreview() {
    SolveLogJetpackComposeTheme {
        SolveLogAddContent(
            onDismiss = {},
            onConfirm = { _, _, _, _, _ -> }
        )
    }
}