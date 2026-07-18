package com.soulhowling.solvelog_jetpackcompose.feature.settings

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import kotlinx.coroutines.launch

@Composable
fun WipeDataScreen(
    viewModel: SettingViewModel = viewModel()
) {
    WipeDataContent(
        onWipeAllData = viewModel::wipeAllData,
        onWipeAllLogs = viewModel::wipeAllLogs,
        onWipeAllTimers = viewModel::wipeAllTimers
    )
}

@Composable
fun WipeDataContent(
    onWipeAllData: suspend () -> Boolean,
    onWipeAllLogs: suspend () -> Boolean,
    onWipeAllTimers: suspend () -> Boolean,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val wipeDataItems = remember(onWipeAllData, onWipeAllLogs, onWipeAllTimers) {
        listOf(
            WipeDataItem.WipeAllData(onWipeAllData),
            WipeDataItem.WipeAllLogs(onWipeAllLogs),
            WipeDataItem.WipeAllTimers(onWipeAllTimers)
        )
    }
    var selectedItem by remember { mutableStateOf<WipeDataItem?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(wipeDataItems) { item ->
            ListItem(
                headlineContent = { Text(text = stringResource(id = item.titleResId)) },
                modifier = Modifier.clickable {
                    selectedItem = item
                }
            )
            HorizontalDivider()
        }
    }

    selectedItem?.let { item ->
        WipeDataDialog(
            title = stringResource(id = item.titleResId),
            text = stringResource(id = R.string.wipe_data_description_prefix) + "\n\n" +
                    stringResource(id = item.descriptionResId) + "\n\n" +
                    stringResource(id = R.string.wipe_data_description_suffix),
            onDismiss = { selectedItem = null },
            onConfirm = {
                scope.launch {
                    val success = item.action?.invoke() ?: false
                    val messageResId = if (success) R.string.wipe_data_success else R.string.wipe_data_failed
                    Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
                    selectedItem = null
                }
            }
        )
    }
}

@Composable
fun WipeDataDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(text = stringResource(id = R.string.common_dialog_ok_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = stringResource(id = R.string.common_dialog_cancel_button))
            }
        },
        onDismissRequest = onDismiss
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WipeDataContentPreview() {
    SolveLogJetpackComposeTheme {
        WipeDataContent(
            onWipeAllData = { true },
            onWipeAllLogs = { true },
            onWipeAllTimers = { true }
        )
    }
}