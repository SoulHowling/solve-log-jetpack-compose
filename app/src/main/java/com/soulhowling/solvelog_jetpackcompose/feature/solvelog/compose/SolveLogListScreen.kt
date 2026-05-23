package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.LocalBottomBarVisibility
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel.SolveLogListItem
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel.SolveLogListViewModel
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun SolveLogListScreen(
    onNavigateToViewer: () -> Unit,
    viewModel: SolveLogListViewModel = viewModel()
) {
    val displayItems by viewModel.displayItems.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val currentFolderPath by viewModel.currentFolderPath.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    SolveLogListScreenContent(
        displayItems = displayItems,
        folders = folders,
        currentFolderPath = currentFolderPath,
        onNavigateToViewer = onNavigateToViewer,
        onSelectFolder = { viewModel.selectFolder(it) },
        onNavigateBack = { viewModel.navigateBack() },
        onAddSolveLog = { name, quantity, selectionType, selectionQuantity ->
            viewModel.insertSolveLog(name, quantity, selectionType, selectionQuantity)
        },
        onAddFolder = { viewModel.insertFolder(it) },
        onDeleteSolveLog = {
            viewModel.deleteSolveLog(it)
            Toast.makeText(context, "「${it.name}」ログを削除しました", Toast.LENGTH_SHORT).show()
        },
        onDeleteFolder = {
            viewModel.deleteFolder(it)
            Toast.makeText(context, "「${it.name}」フォルダを削除しました", Toast.LENGTH_SHORT).show()
        },
        onMoveSolveLog = { log, folderId ->
            viewModel.moveSolveLog(log, folderId)
            Toast.makeText(context, "移動しました", Toast.LENGTH_SHORT).show()
        },
        onMoveFolder = { folder, targetFolderId ->
            viewModel.moveFolder(folder, targetFolderId)
            Toast.makeText(context, "移動しました", Toast.LENGTH_SHORT).show()
        }
    )
}

@Composable
private fun SolveLogListScreenContent(
    displayItems: List<SolveLogListItem>,
    folders: List<Folder>,
    currentFolderPath: String?,
    onNavigateToViewer: () -> Unit,
    onSelectFolder: (Int?) -> Unit,
    onNavigateBack: () -> Unit,
    onAddSolveLog: (String, Int, SelectionType, SelectionQuantity) -> Unit,
    onAddFolder: (String) -> Unit,
    onDeleteSolveLog: (SolveLog) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    onMoveSolveLog: (SolveLog, Int?) -> Unit,
    onMoveFolder: (Folder, Int?) -> Unit
) {
    var isShownBottomSheet by remember { mutableStateOf(false) }
    var isShownAddFolderDialog by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }
    
    var movingLog by remember { mutableStateOf<SolveLog?>(null) }
    var movingFolder by remember { mutableStateOf<Folder?>(null) }

    val isBottomBarVisible = LocalBottomBarVisibility.current

    val fabTranslationY by animateDpAsState(
        targetValue = if (isBottomBarVisible) (-80).dp else 0.dp,
        label = "fabTranslationY"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // フォルダ内にいる場合、戻るボタンとフォルダ名を表示
            if (currentFolderPath != null) {
                CurrentFolderHeader(
                    currentFolderPath = currentFolderPath,
                    onNavigateBack = onNavigateBack
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = Dimensions.PADDING_MEDIUM,
                    top = Dimensions.PADDING_MEDIUM,
                    end = Dimensions.PADDING_MEDIUM,
                    bottom = Dimensions.LIST_BOTTOM_WITH_FAB
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING_MEDIUM)
            ) {
                items(displayItems.size) { index ->
                    when (val item = displayItems[index]) {
                        is SolveLogListItem.FolderItem -> {
                            FolderItemCard(
                                title = item.folder.name,
                                onClick = { onSelectFolder(item.folder.id) },
                                onDelete = { onDeleteFolder(item.folder) },
                                onMove = { movingFolder = item.folder }
                            )
                        }
                        is SolveLogListItem.LogItem -> {
                            SolveLogItemCard(
                                title = item.log.name,
                                description = "問題数: ${item.log.questionQuantity}",
                                attemptCount = item.log.attemptCount,
                                onClick = onNavigateToViewer,
                                onDelete = { onDeleteSolveLog(item.log) },
                                onMove = { movingLog = item.log }
                            )
                        }
                    }
                }
            }
        }

        // 背景を暗くするオーバーレイ
        if (isFabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                    .clickable { isFabExpanded = false }
            )
        }

        NewActionButtons(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .graphicsLayer {
                    this.translationY = fabTranslationY.toPx()
                }
                .padding(Dimensions.PADDING_MEDIUM),
            expanded = isFabExpanded,
            onToggle = { isFabExpanded = !isFabExpanded },
            onAddNewFolder = {
                isFabExpanded = false
                isShownAddFolderDialog = true
            },
            onAddNewLog = {
                isFabExpanded = false
                isShownBottomSheet = true
            }
        )
        
        if (isShownAddFolderDialog) {
            FolderAddDialog(
                onDismiss = { isShownAddFolderDialog = false},
                onConfirm = { name ->
                    isShownAddFolderDialog = false
                    onAddFolder(name)
                }
            )
        }

        if (isShownBottomSheet) {
            SolveLogAddBottomSheet(
                onDismiss = { isShownBottomSheet = false },
                onAddSolveLog = onAddSolveLog
            )
        }

        movingLog?.let { log ->
            MoveToFolderDialog(
                allFolders = folders,
                onDismiss = { movingLog = null },
                onConfirm = { targetFolderId ->
                    onMoveSolveLog(log, targetFolderId)
                    movingLog = null
                }
            )
        }

        movingFolder?.let { folder ->
            MoveToFolderDialog(
                allFolders = folders,
                excludeFolderId = folder.id,
                onDismiss = { movingFolder = null },
                onConfirm = { targetFolderId ->
                    onMoveFolder(folder, targetFolderId)
                    movingFolder = null
                }
            )
        }
    }
}

@Composable
private fun FolderItemCard(
    title: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.SPACING_SMALL),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(Dimensions.PADDING_MEDIUM)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = Dimensions.PADDING_MEDIUM)
                    .size(40.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            OverFlowMenu(onDelete = onDelete, onMove = onMove)
        }
    }
}

@Composable
private fun SolveLogItemCard(
    title: String,
    description: String,
    attemptCount: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.SPACING_SMALL),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(Dimensions.PADDING_MEDIUM)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Assignment,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = Dimensions.PADDING_MEDIUM)
                    .size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Row(
                    modifier = Modifier
                ) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = Dimensions.PADDING_SMALL)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "試行回数：$attemptCount",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = Dimensions.PADDING_SMALL)
                    )
                }

            }
            OverFlowMenu(onDelete = onDelete, onMove = onMove)
        }
    }
}

@Composable
private fun OverFlowMenu(
    onDelete: () -> Unit,
    onMove: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { isExpanded = true }
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null
            )
        }
        DropdownMenu(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .clip(RoundedCornerShape(16.dp)),
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            DropdownMenuItem(
                onClick = {
                    onMove()
                    isExpanded = false
                },
                text = {
                    Text(
                        text = "移動",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                    )
                }
            )
            DropdownMenuItem(
                onClick = {
                    onDelete()
                    isExpanded = false
                },
                text = {
                    Text(
                        text = "削除",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                    )
                }
            )
        }
    }
}

@Composable
fun CurrentFolderHeader(
    currentFolderPath: String,
    onNavigateBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.PADDING_MEDIUM, vertical = Dimensions.PADDING_SMALL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onNavigateBack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
        }
        Text(
            text = currentFolderPath,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = Dimensions.PADDING_SMALL)
        )
    }
}

@Composable
private fun NewActionButtons(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddNewFolder: () -> Unit,
    onAddNewLog: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // フォルダ作成ボタン
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Text(
                            text = "フォルダ",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onAddNewFolder,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "フォルダを作成")
                    }
                }

                // ログ作成ボタン
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Text(
                            text = "ログ",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onAddNewLog,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(Icons.Default.PostAdd, contentDescription = "ログを作成")
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            containerColor = if (expanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
        ) {
            val rotation by animateFloatAsState(if (expanded) 45f else 0f, label = "fabRotation")
            Icon(
                Icons.Filled.Add,
                contentDescription = if (expanded) "メニューを閉じる" else "新規作成メニューを開く",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
private fun FolderAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var folderTitle by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新規フォルダーを作成") },
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
                    onConfirm(folderTitle)
                }
            ) {
                Text(stringResource(R.string.common_dialog_confirm_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) { 
                Text(stringResource(R.string.common_dialog_cancel_button))
            }
        }
    )
}

@Composable
private fun FolderAddContent(
    folderTitle: String,
    onValueChanged: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING_SMALL_MEDIUM),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "フォルダ名",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        TextField(
            value = folderTitle,
            onValueChange = {
                onValueChanged(it)
            },
            placeholder = { Text("例: 統計調査士") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SolveLogAddBottomSheet(
    onDismiss: () -> Unit,
    onAddSolveLog: (String, Int, SelectionType, SelectionQuantity) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
    ) {
        SolveLogAddContent(
            onDismiss = onDismiss,
            onAddSolveLog = onAddSolveLog
        )
    }
}


@Composable
private fun SolveLogAddContent(
    onDismiss: () -> Unit,
    onAddSolveLog: (String, Int, SelectionType, SelectionQuantity) -> Unit
) {
    val listState = rememberLazyListState()
    val itemKindList = SelectionType.entries.map { it.displayString }
    val itemQuantityList = SelectionQuantity.entries.map { it.displayString }

    var selectedKind by rememberSaveable { mutableStateOf(SelectionType.NUMBER) }
    var selectedQuantity by rememberSaveable { mutableStateOf(SelectionQuantity.FOUR) }

    val isTrueFalseSelected = selectedKind == SelectionType.CORRECTNESS

    val sectionSpace = Dimensions.SPACING_MEDIUM

    var logTitle by rememberSaveable { mutableStateOf("") }
    var questionQuantity by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.PADDING_MEDIUM)
    ) {
        item {
            Text(
                text = "新しいログを追加",
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
                    text = "ログタイトル",
                    style = MaterialTheme.typography.titleMedium,
                )
                TextField(
                    value = logTitle,
                    onValueChange = {
                        logTitle = it
                    },
                    placeholder = { Text("例: 統計調査士 2019年過去問") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(sectionSpace))

                Text(
                    text = "問題数",
                    style = MaterialTheme.typography.titleMedium
                )
                TextField(
                    value = questionQuantity,
                    onValueChange = {
                        questionQuantity = it
                    },
                    placeholder = { Text("例: 100") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(sectionSpace))


                Text(
                    text = "選択肢の種類",
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
                    text = "選択肢の数",
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
                    onAddSolveLog(
                        logTitle,
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
                Text("追加")
            }
        }

        item {
            Spacer(modifier = Modifier.height(Dimensions.SPACING_LARGE))
        }
    }
}

private fun isAddButtonEnabled(logTitle: String, questionQuantity: String): Boolean {
    return logTitle.isNotBlank() && questionQuantity.isNotBlank() && questionQuantity.toIntOrZero() > 0
}

private fun String.toIntOrZero(): Int {
    return toIntOrNull() ?: 0
}

@Composable
private fun SingleChoiceSegmentedButton(
    modifier: Modifier = Modifier,
    itemList: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    isEnabled: Boolean
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier,
    ) {
        itemList.forEachIndexed { index, label ->
            SegmentedButton(
                enabled = isEnabled,
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = itemList.size
                ),
                onClick = { onSelectedIndexChange(index) },
                selected = index == selectedIndex,
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun MoveToFolderDialog(
    allFolders: List<Folder>,
    excludeFolderId: Int? = null,
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit
) {
    var currentViewedFolderId by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("移動先を選択") },
        text = {
            MoveToFolderContent(
                allFolders = allFolders,
                excludeFolderId = excludeFolderId,
                currentViewedFolderId = currentViewedFolderId,
                onViewFolder = { currentViewedFolderId = it }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(currentViewedFolderId) }) {
                Text("ここに移動")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_dialog_cancel_button))
            }
        }
    )
}

@Composable
private fun MoveToFolderContent(
    allFolders: List<Folder>,
    excludeFolderId: Int?,
    currentViewedFolderId: Int?,
    onViewFolder: (Int?) -> Unit
) {
    // 循環参照を避けるためのフィルタリング
    val excludedIds = remember(allFolders, excludeFolderId) {
        val ids = mutableSetOf<Int>()
        if (excludeFolderId != null) {
            ids.add(excludeFolderId)
            var changed = true
            while (changed) {
                val count = ids.size
                allFolders.forEach {
                    if (it.parentFolderId in ids) {
                        ids.add(it.id)
                    }
                }
                changed = ids.size > count
            }
        }
        ids
    }

    val availableFolders = allFolders.filter { it.id !in excludedIds }
    val foldersToShow = availableFolders.filter { it.parentFolderId == currentViewedFolderId }

    val currentViewedFolderPath = remember(currentViewedFolderId, allFolders) {
        if (currentViewedFolderId == null) return@remember "ルート"
        val path = mutableListOf<String>()
        var current: Folder? = allFolders.find { it.id == currentViewedFolderId }
        while (current != null) {
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
                    val parentId = allFolders.find { it.id == currentViewedFolderId }?.parentFolderId
                    onViewFolder(parentId)
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上に戻る")
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
            items(foldersToShow.size) { index ->
                val folder = foldersToShow[index]
                MoveToFolderItem(
                    name = folder.name,
                    onClick = { onViewFolder(folder.id) }
                )
            }
            
            if (foldersToShow.isEmpty()) {
                item {
                    Text(
                        text = "サブフォルダはありません",
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = name, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun SolveLogListScreenContentPreview() {
    val solveLogs = List(10) { index ->
        SolveLog(
            id = index,
            name = "テスト#$index",
            questionQuantity = 30,
            selectionType = SelectionType.ALPHABET.index,
            selectionQuantity = SelectionQuantity.FOUR.index,
            attemptCount = 1,
            createdAt = 0L,
            updatedAt = 0L,
            folderId = null
        )
    }
    val displayItems = solveLogs.map { SolveLogListItem.LogItem(it) }
    SolveLogJetpackComposeTheme {
        SolveLogListScreenContent(
            displayItems = displayItems,
            folders = emptyList(),
            currentFolderPath = "テスト / サブフォルダ",
            onNavigateToViewer = {},
            onSelectFolder = {},
            onNavigateBack = {},
            onAddSolveLog = { _, _, _, _ -> },
            onAddFolder = {},
            onDeleteSolveLog = {},
            onDeleteFolder = {},
            onMoveSolveLog = { _, _ -> },
            onMoveFolder = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SolveLogAddContentPreview() {
    SolveLogJetpackComposeTheme {
        SolveLogAddContent(
            onDismiss = {},
            onAddSolveLog = { _, _, _, _ -> }
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

@Preview(showBackground = true)
@Composable
fun MoveToFolderContentPreview() {
    val createdAt = System.currentTimeMillis()
    val folders = listOf(
        Folder(id = 1, name = "フォルダA", createdAt = createdAt, updatedAt = createdAt, parentFolderId = null),
        Folder(id = 2, name = "フォルダB", createdAt = createdAt, updatedAt = createdAt, parentFolderId = null),
        Folder(id = 3, name = "サブフォルダA-1", createdAt = createdAt, updatedAt = createdAt, parentFolderId = 1),
        Folder(id = 4, name = "サブフォルダA-2", createdAt = createdAt, updatedAt = createdAt, parentFolderId = 1),
        Folder(id = 5, name = "サブフォルダB-1", createdAt = createdAt, updatedAt = createdAt, parentFolderId = 2)
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
