package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import com.soulhowling.solvelog_jetpackcompose.utils.compose.SingleChoiceSegmentedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolveLogAddBottomSheet(
    initialName: String = "",
    initialQuestionQuantity: Int? = null,
    initialSelectionType: SelectionType = SelectionType.NUMBER,
    initialSelectionQuantity: SelectionQuantity = SelectionQuantity.FOUR,
    titleResId: Int = R.string.log_list_add_new_log_title,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, SelectionType, SelectionQuantity) -> Unit
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
            initialSelectionType = initialSelectionType,
            initialSelectionQuantity = initialSelectionQuantity,
            titleResId = titleResId,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun SolveLogAddContent(
    initialName: String = "",
    initialQuestionQuantity: Int? = null,
    initialSelectionType: SelectionType = SelectionType.NUMBER,
    initialSelectionQuantity: SelectionQuantity = SelectionQuantity.FOUR,
    titleResId: Int = R.string.log_list_add_new_log_title,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, SelectionType, SelectionQuantity) -> Unit
) {
    val listState = rememberLazyListState()
    val itemKindList = SelectionType.entries.map { it.displayString }
    val itemQuantityList = SelectionQuantity.entries.map { it.displayString }

    var selectedKind by rememberSaveable(initialSelectionType) { mutableStateOf(initialSelectionType) }
    var selectedQuantity by rememberSaveable(initialSelectionQuantity) { mutableStateOf(initialSelectionQuantity) }

    val isTrueFalseSelected = selectedKind == SelectionType.CORRECTNESS

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
                    modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(sectionSpace))


                Text(
                    text = stringResource(R.string.log_list_label_selection_type),
                    style = MaterialTheme.typography.titleMedium
                )
                SingleChoiceSegmentedButton(
                    modifier = Modifier.fillMaxWidth(),
                    itemList = itemKindList,
                    selectedIndex = selectedKind.index,
                    onSelectedIndexChange = { selectedKind = SelectionType.entries[it] },
                    isEnabled = true
                )
                Spacer(modifier = Modifier.height(sectionSpace))


                Text(
                    text = stringResource(R.string.log_list_label_selection_quantity),
                    style = MaterialTheme.typography.titleMedium
                )
                SingleChoiceSegmentedButton(
                    modifier = Modifier.fillMaxWidth(),
                    itemList = itemQuantityList,
                    selectedIndex = if (isTrueFalseSelected) SelectionQuantity.TWO.index else selectedQuantity.index,
                    onSelectedIndexChange = { selectedQuantity = SelectionQuantity.entries[it] },
                    isEnabled = !isTrueFalseSelected
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
                        selectedKind,
                        if (isTrueFalseSelected) SelectionQuantity.TWO else selectedQuantity
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
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
            onConfirm = { _, _, _, _ -> }
        )
    }
}