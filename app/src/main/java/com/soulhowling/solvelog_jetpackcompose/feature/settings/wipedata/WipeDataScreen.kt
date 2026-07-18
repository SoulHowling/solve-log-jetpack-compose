package com.soulhowling.solvelog_jetpackcompose.feature.settings.wipedata

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.feature.settings.WipeDataItem
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun WipeDataScreen(
    viewModel: WipeDataViewModel = viewModel()
) {
    val wipeStatus by viewModel.wipeStatus.collectAsState()

    WipeDataContent(
        wipeStatus = wipeStatus,
        onWipeConfirm = viewModel::onWipeConfirm,
        onResetWipeStatus = viewModel::resetWipeStatus
    )
}

@Composable
fun WipeDataContent(
    wipeStatus: WipeDataViewModel.WipeStatus,
    onWipeConfirm: (WipeDataItem) -> Unit,
    onResetWipeStatus: () -> Unit,
) {
    val context = LocalContext.current
    val wipeDataItems = remember {
        listOf(
            WipeDataItem.WipeAllData,
            WipeDataItem.WipeAllLogs,
            WipeDataItem.WipeAllTimers
        )
    }
    var selectedItem by remember { mutableStateOf<WipeDataItem?>(null) }

    LaunchedEffect(wipeStatus) {
        when (wipeStatus) {
            is WipeDataViewModel.WipeStatus.Success -> {
                Toast.makeText(context, R.string.wipe_data_success, Toast.LENGTH_SHORT).show()
                onResetWipeStatus()
            }
            is WipeDataViewModel.WipeStatus.Error -> {
                Toast.makeText(context, R.string.wipe_data_failed, Toast.LENGTH_SHORT).show()
                onResetWipeStatus()
            }
            else -> {}
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = wipeDataItems,
            key = { it.titleResId }
        ) { item ->
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
                selectedItem = null
                onWipeConfirm(item)
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
            wipeStatus = WipeDataViewModel.WipeStatus.Idle,
            onWipeConfirm = {},
            onResetWipeStatus = {}
        )
    }
}