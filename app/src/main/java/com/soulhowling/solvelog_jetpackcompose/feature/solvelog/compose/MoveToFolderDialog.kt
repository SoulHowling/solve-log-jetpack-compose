package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

object MoveToFolderDialogTestTags {
    const val BACK_BUTTON = "move_to_folder_back_button"
    const val FOLDER_ITEM = "move_to_folder_item_"
    const val CONFIRM_BUTTON = "move_to_folder_confirm_button"
    const val CANCEL_BUTTON = "move_to_folder_cancel_button"
}

@Composable
fun MoveToFolderDialog(
    allFolders: List<Folder>,
    excludeFolderId: Int? = null,
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit
) {
    var currentViewedFolderId by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.log_list_dialog_move_to_folder_title)) },
        text = {
            MoveToFolderContent(
                allFolders = allFolders,
                excludeFolderId = excludeFolderId,
                currentViewedFolderId = currentViewedFolderId,
                onViewFolder = { currentViewedFolderId = it }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(currentViewedFolderId) }, modifier = Modifier.testTag(MoveToFolderDialogTestTags.CONFIRM_BUTTON)) {
                Text(stringResource(R.string.log_list_dialog_move_here_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag(MoveToFolderDialogTestTags.CANCEL_BUTTON)) {
                Text(stringResource(R.string.common_dialog_cancel_button))
            }
        }
    )
}

@Composable
internal fun MoveToFolderContent(
    allFolders: List<Folder>,
    excludeFolderId: Int?,
    currentViewedFolderId: Int?,
    onViewFolder: (Int?) -> Unit
) {
    val excludedIds = remember(allFolders, excludeFolderId) {
        val ids = mutableSetOf<Int>()
        if (excludeFolderId != null) {
            ids.add(excludeFolderId)
            val parentToChildren = allFolders.groupBy { it.parentFolderId }
            val queue = ArrayDeque<Int>().apply { add(excludeFolderId) }
            while (queue.isNotEmpty()) {
                val currentId = queue.removeFirst()
                parentToChildren[currentId]?.forEach { child ->
                    if (ids.add(child.id)) {
                        queue.add(child.id)
                    }
                }
            }
        }
        ids
    }

    val availableFolders = allFolders.filter { it.id !in excludedIds }
    val foldersToShow = availableFolders.filter { it.parentFolderId == currentViewedFolderId }

    val rootFolderName = stringResource(R.string.log_list_root_folder)
    val currentViewedFolderPath = remember(currentViewedFolderId, allFolders, rootFolderName) {
        if (currentViewedFolderId == null) return@remember rootFolderName
        val path = mutableListOf<String>()
        val visited = mutableSetOf<Int>()
        var current: Folder? = allFolders.find { it.id == currentViewedFolderId }
        while (current != null && visited.add(current.id)) {
            path.add(0, current.name)
            current = allFolders.find { it.id == current.parentFolderId }
        }
        path.joinToString(" / ")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // 現在のパス表示と戻るボタン
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentViewedFolderId != null) {
                IconButton(onClick = {
                    val parentId =
                        allFolders.find { it.id == currentViewedFolderId }?.parentFolderId
                    onViewFolder(parentId)
                }, modifier = Modifier.testTag(MoveToFolderDialogTestTags.BACK_BUTTON)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.log_list_cd_go_up)
                    )
                }
            }
            Text(
                text = currentViewedFolderPath,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(
                items = foldersToShow,
                key = { it.id }
            ) { folder ->
                MoveToFolderItem(
                    name = folder.name,
                    onClick = { onViewFolder(folder.id) },
                    modifier = Modifier.testTag(MoveToFolderDialogTestTags.FOLDER_ITEM + folder.id)
                )
            }

            if (foldersToShow.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.log_list_no_subfolders),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MoveToFolderItem(
    name: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Folder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = name, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun MoveToFolderContentPreview() {
    val createdAt = System.currentTimeMillis()
    val folders = listOf(
        Folder(
            id = 1,
            name = "フォルダA",
            createdAt = createdAt,
            updatedAt = createdAt,
            parentFolderId = null
        ),
        Folder(
            id = 2,
            name = "フォルダB",
            createdAt = createdAt,
            updatedAt = createdAt,
            parentFolderId = null
        ),
        Folder(
            id = 3,
            name = "サブフォルダA-1",
            createdAt = createdAt,
            updatedAt = createdAt,
            parentFolderId = 1
        ),
        Folder(
            id = 4,
            name = "サブフォルダA-2",
            createdAt = createdAt,
            updatedAt = createdAt,
            parentFolderId = 1
        ),
        Folder(
            id = 5,
            name = "サブフォルダB-1",
            createdAt = createdAt,
            updatedAt = createdAt,
            parentFolderId = 2
        )
    )
    SolveLogJetpackComposeTheme {
        MoveToFolderContent(
            allFolders = folders,
            excludeFolderId = null,
            currentViewedFolderId = 3,
            onViewFolder = {}
        )
    }
}