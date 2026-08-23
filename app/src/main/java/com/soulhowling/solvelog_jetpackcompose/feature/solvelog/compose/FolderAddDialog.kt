package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

object FolderAddDialogTestTags {
    const val TEXT_FIELD = "folder_add_text_field"
    const val CONFIRM_BUTTON = "folder_add_confirm_button"
    const val CANCEL_BUTTON = "folder_add_cancel_button"
}

@Composable
fun FolderAddDialog(
    initialName: String = "",
    titleResId: Int = R.string.log_list_add_new_folder_title,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var folderTitle by rememberSaveable(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleResId)) },
        text = {
            FolderAddContent(
                folderTitle = folderTitle,
                onValueChanged = {
                    folderTitle = it
                }
            )
        },
        confirmButton = {
            TextButton(
                enabled = folderTitle.isNotBlank(),
                onClick = {
                    onConfirm(folderTitle.trim())
                },
                modifier = Modifier.testTag(FolderAddDialogTestTags.CONFIRM_BUTTON)
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
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(FolderAddDialogTestTags.CANCEL_BUTTON)
            ) {
                Text(stringResource(R.string.common_dialog_cancel_button))
            }
        }
    )
}

@Composable
fun FolderAddContent(
    folderTitle: String,
    onValueChanged: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING_SMALL_MEDIUM),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.log_list_label_folder_name),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        TextField(
            value = folderTitle,
            onValueChange = {
                onValueChanged(it)
            },
            placeholder = { Text(stringResource(R.string.log_list_placeholder_folder_name)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(FolderAddDialogTestTags.TEXT_FIELD)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FolderAddContentPreview() {
    SolveLogJetpackComposeTheme {
        FolderAddContent(
            folderTitle = "",
            onValueChanged = {}
        )
    }
}