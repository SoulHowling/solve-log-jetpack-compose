package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

object FreeAnswerInputDialogTestTags {
    const val TEXT_FIELD = "free_answer_input_text_field"
    const val CONFIRM_BUTTON = "free_answer_input_confirm_button"
    const val CANCEL_BUTTON = "free_answer_input_cancel_button"
}

@Composable
fun FreeAnswerInputDialog(
    title: String,
    content: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var inputText by rememberSaveable(content) { mutableStateOf(content) }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false
        ),
        title = { Text(text = title) },
        text = {
            FreeAnswerContent(
                content = inputText,
                onValueChanged = {
                    inputText = it
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(inputText)
                },
                modifier = Modifier.testTag(FreeAnswerInputDialogTestTags.CONFIRM_BUTTON)
            ) {
                Text(
                    stringResource(
                        R.string.common_dialog_save_button
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(FreeAnswerInputDialogTestTags.CANCEL_BUTTON)
            ) {
                Text(stringResource(R.string.common_dialog_cancel_button))
            }
        }
    )
}

@Composable
fun FreeAnswerContent(
    content: String,
    onValueChanged: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING_SMALL_MEDIUM),
        horizontalAlignment = Alignment.Start
    ) {
        OutlinedTextField(
            label = { Text(stringResource(R.string.log_free_answer_dialog_input_label)) },
            value = content,
            onValueChange = {
                onValueChanged(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(FreeAnswerInputDialogTestTags.TEXT_FIELD),
        )
        Text(
            text = stringResource(R.string.log_free_answer_dialog_input_text_count, content.length),
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FreeAnswerInputDialogPreview() {
    SolveLogJetpackComposeTheme {
        FreeAnswerInputDialog(
            title = "12",
            content = "解答",
            onConfirm = {},
            onDismiss = {}
        )
    }
}