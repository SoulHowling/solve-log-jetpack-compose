package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.CustomAppBarEffect
import com.soulhowling.solvelog_jetpackcompose.core.LocalAppBarController
import com.soulhowling.solvelog_jetpackcompose.core.LocalBottomBarVisibility
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel.ExportTarget
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel.SolveLogListItem
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel.SolveLogListViewModel
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun SolveLogListScreen(
    onNavigateToViewer: (Int) -> Unit,
    viewModel: SolveLogListViewModel = viewModel()
) {
    val displayItems by viewModel.displayItems.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val currentFolderPath by viewModel.currentFolderPath.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingExportTarget by rememberSaveable { mutableStateOf<ExportTarget?>(null) }
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val target = pendingExportTarget ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            viewModel.performExport(uri, target) { success ->
                if (success) {
                    Toast.makeText(context, R.string.common_export_success, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(context, R.string.common_export_failed, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        pendingExportTarget = null
    }

    LaunchedEffect(Unit) {
        viewModel.exportTarget.collect { target ->
            pendingExportTarget = target
            createDocumentLauncher.launch(target.fileName)
        }
    }

    val deleteLogSuccessFormat = stringResource(R.string.log_list_delete_log_success)
    val deleteFolderSuccessFormat = stringResource(R.string.log_list_delete_folder_success)
    val moveSuccessMessage = stringResource(R.string.log_list_move_success)
    var isExpanded by remember { mutableStateOf(false) }

    CustomAppBarEffect(
        title = null,
        actions = {
            IconButton(
                onClick = { isExpanded = true }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            DropdownMenu(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background),
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        viewModel.exportAll()
                        isExpanded = false
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.log_list_dropdown_menu_item_all_export),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                        )
                    }
                )
            }
        }
    )

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
            Toast.makeText(
                context,
                deleteLogSuccessFormat.format(it.name),
                Toast.LENGTH_SHORT
            ).show()
        },
        onDeleteFolder = {
            viewModel.deleteFolder(it)
            Toast.makeText(
                context,
                deleteFolderSuccessFormat.format(it.name),
                Toast.LENGTH_SHORT
            ).show()
        },
        onMoveSolveLog = { log, folderId ->
            viewModel.moveSolveLog(log, folderId)
            Toast.makeText(
                context,
                moveSuccessMessage,
                Toast.LENGTH_SHORT
            ).show()
        },
        onMoveFolder = { folder, targetFolderId ->
            viewModel.moveFolder(folder, targetFolderId)
            Toast.makeText(
                context,
                moveSuccessMessage,
                Toast.LENGTH_SHORT
            ).show()
        },
        onExportLog = { log ->
            viewModel.export(log)
        },
        onExportFolder = { folder ->
            viewModel.export(folder)
        }
    )
}

@Composable
private fun SolveLogListScreenContent(
    displayItems: List<SolveLogListItem>,
    folders: List<Folder>,
    currentFolderPath: String?,
    onNavigateToViewer: (Int) -> Unit,
    onSelectFolder: (Int?) -> Unit,
    onNavigateBack: () -> Unit,
    onAddSolveLog: (String, Int, SelectionType, SelectionQuantity) -> Unit,
    onAddFolder: (String) -> Unit,
    onDeleteSolveLog: (SolveLog) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    onMoveSolveLog: (SolveLog, Int?) -> Unit,
    onMoveFolder: (Folder, Int?) -> Unit,
    onExportLog: (SolveLog) -> Unit,
    onExportFolder: (Folder) -> Unit,
) {
    val questionQuantityFormatText = stringResource(R.string.log_list_title_question_quantity)
    var isShownBottomSheet by remember { mutableStateOf(false) }
    var isShownAddFolderDialog by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val appBarController = LocalAppBarController.current

    LaunchedEffect(listState) {
        snapshotFlow { listState.canScrollForward || listState.canScrollBackward }
            .collect { canScroll ->
                if (!canScroll) {
                    appBarController.setBottomBarVisible(true)
                }
            }
    }

    var movingLog by remember { mutableStateOf<SolveLog?>(null) }
    var movingFolder by remember { mutableStateOf<Folder?>(null) }

    val isBottomBarVisible = LocalBottomBarVisibility.current

    val fabTranslationY by animateDpAsState(
        targetValue = if (isBottomBarVisible) (-80).dp else 0.dp,
        label = "fabTranslationY"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (currentFolderPath != null) {
                CurrentFolderHeader(
                    currentFolderPath = currentFolderPath,
                    onNavigateBack = onNavigateBack
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = Dimensions.PADDING_MEDIUM,
                    top = Dimensions.PADDING_MEDIUM,
                    end = Dimensions.PADDING_MEDIUM,
                    bottom = Dimensions.LIST_BOTTOM_WITH_FAB
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING_MEDIUM)
            ) {
                items(
                    items = displayItems,
                    key = { item ->
                        when (item) {
                            is SolveLogListItem.FolderItem -> "folder_${item.folder.id}"
                            is SolveLogListItem.LogItem -> "log_${item.log.id}"
                        }
                    }
                ) { item ->
                    when (item) {
                        is SolveLogListItem.FolderItem -> {
                            FolderItemCard(
                                title = item.folder.name,
                                onClick = { onSelectFolder(item.folder.id) },
                                onDelete = { onDeleteFolder(item.folder) },
                                onMove = { movingFolder = item.folder },
                                onExportFolder = { onExportFolder(item.folder) }
                            )
                        }

                        is SolveLogListItem.LogItem -> {
                            SolveLogItemCard(
                                title = item.log.name,
                                description = questionQuantityFormatText.format(item.log.questionQuantity),
                                attemptCount = item.log.attemptCount,
                                onClick = { onNavigateToViewer(item.log.id) },
                                onDelete = { onDeleteSolveLog(item.log) },
                                onMove = { movingLog = item.log },
                                onExportLog = { onExportLog(item.log) }
                            )
                        }
                    }
                }
            }
        }

        // Overlay
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
                onDismiss = { isShownAddFolderDialog = false },
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
    onMove: () -> Unit,
    onExportFolder: () -> Unit,
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
            OverFlowMenu(
                onDelete = onDelete,
                onMove = onMove,
                onExport = onExportFolder,
            )
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
    onMove: () -> Unit,
    onExportLog: () -> Unit,
) {
    val attemptCountFormatText = stringResource(R.string.log_list_title_attempt_count)
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
                        text = attemptCountFormatText.format(attemptCount),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = Dimensions.PADDING_SMALL)
                    )
                }

            }
            OverFlowMenu(
                onDelete = onDelete,
                onMove = onMove,
                onExport = onExportLog,
            )
        }
    }
}

@Composable
private fun OverFlowMenu(
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onExport: () -> Unit,
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
                        text = stringResource(R.string.log_list_dropdown_menu_item_move),
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
                        text = stringResource(R.string.log_list_dropdown_menu_item_delete),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                    )
                }
            )
            DropdownMenuItem(
                onClick = {
                    onExport()
                    isExpanded = false
                },
                text = {
                    Text(
                        text = stringResource(R.string.log_list_dropdown_menu_item_export),
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
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.log_list_cd_back)
            )
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
                            text = stringResource(R.string.log_list_add_folder_button),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onAddNewFolder,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(
                            Icons.Default.CreateNewFolder,
                            contentDescription = stringResource(R.string.log_list_cd_add_folder)
                        )
                    }
                }

                // Create log button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.log_list_add_log_button),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onAddNewLog,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(
                            Icons.Default.PostAdd,
                            contentDescription = stringResource(R.string.log_list_cd_add_log)
                        )
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
                contentDescription = if (expanded) stringResource(R.string.log_list_cd_close_menu) else stringResource(
                    R.string.log_list_cd_open_menu
                ),
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
        title = { Text(stringResource(R.string.log_list_add_new_folder_title)) },
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
                text = stringResource(R.string.log_list_add_new_log_title),
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
                Text(stringResource(R.string.log_list_add_button))
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
            TextButton(onClick = { onConfirm(currentViewedFolderId) }) {
                Text(stringResource(R.string.log_list_dialog_move_here_button))
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
                }) {
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
                    onClick = { onViewFolder(folder.id) }
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
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
            onMoveFolder = { _, _ -> },
            onExportLog = {},
            onExportFolder = {}
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
