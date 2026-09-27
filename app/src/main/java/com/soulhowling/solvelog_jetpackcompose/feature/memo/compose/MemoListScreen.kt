package com.soulhowling.solvelog_jetpackcompose.feature.memo.compose

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.CustomAppBarEffect
import com.soulhowling.solvelog_jetpackcompose.core.LocalAppBarController
import com.soulhowling.solvelog_jetpackcompose.core.LocalBottomBarVisibility
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.Memo
import com.soulhowling.solvelog_jetpackcompose.feature.memo.viewmodel.MemoExportTarget
import com.soulhowling.solvelog_jetpackcompose.feature.memo.viewmodel.MemoListItem
import com.soulhowling.solvelog_jetpackcompose.feature.memo.viewmodel.MemoListViewModel
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.ViewType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose.FolderAddDialog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose.MoveToFolderDialog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import com.soulhowling.solvelog_jetpackcompose.utils.compose.CurrentFolderHeader
import com.soulhowling.solvelog_jetpackcompose.utils.compose.ItemCard
import com.soulhowling.solvelog_jetpackcompose.utils.compose.ItemDragIndicator
import com.soulhowling.solvelog_jetpackcompose.utils.compose.ItemOverflowMenu
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MemoListScreen(
    onNavigateToView: (Int) -> Unit,
    onNavigateToEditor: (Int, Int?) -> Unit,
    viewModel: MemoListViewModel = viewModel()
) {
    val displayItems by viewModel.displayItems.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val currentFolderPath by viewModel.currentFolderPath.collectAsStateWithLifecycle()
    val currentFolderId by viewModel.currentFolderId.collectAsStateWithLifecycle()
    val viewType by viewModel.viewType.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingExportTarget by remember { mutableStateOf<MemoExportTarget?>(null) }
    val exportDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val target = pendingExportTarget ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            viewModel.performExport(uri, target) { success ->
                if (success) {
                    Toast.makeText(context, R.string.common_export_success, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.common_export_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
        pendingExportTarget = null
    }

    val importDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.performImport(uri) { success ->
                if (success) {
                    Toast.makeText(context, R.string.common_import_success, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.common_import_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.exportTarget.collect { target ->
            pendingExportTarget = target
            exportDocumentLauncher.launch(target.fileName)
        }
    }

    var isExpanded by remember { mutableStateOf(false) }
    var isSortMode by remember { mutableStateOf(false) }

    CustomAppBarEffect(
        title = if (isSortMode) stringResource(R.string.log_list_dropdown_menu_item_sort) else stringResource(R.string.memo_title),
        actions = {
            if (isSortMode) {
                TextButton(onClick = { isSortMode = false }) {
                    Text(
                        text = stringResource(R.string.common_dialog_ok_button),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                IconButton(onClick = { viewModel.toggleViewMode() }) {
                    Icon(
                        imageVector = viewType.nextButtonImage,
                        contentDescription = stringResource(R.string.log_list_cd_toggle_view),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(onClick = { isExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.log_list_cd_more_options),
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
                            isSortMode = true
                            isExpanded = false
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.log_list_dropdown_menu_item_sort),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                    DropdownMenuItem(
                        onClick = {
                            viewModel.exportAllMemos()
                            isExpanded = false
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.log_list_dropdown_menu_item_all_export),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                    DropdownMenuItem(
                        onClick = {
                            importDocumentLauncher.launch(arrayOf("application/json"))
                            isExpanded = false
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.log_list_dropdown_menu_item_import),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                }
            }
        }
    )

    MemoListScreenContent(
        displayItems = displayItems,
        folders = folders,
        currentFolderPath = currentFolderPath,
        isCardView = viewType == ViewType.CARD,
        isSortMode = isSortMode,
        onMemoClick = { onNavigateToView(it.id) },
        onSelectFolder = { viewModel.selectFolder(it) },
        onNavigateBack = { viewModel.navigateBack() },
        onAddMemoClick = { onNavigateToEditor(-1, currentFolderId) },
        onAddFolder = { viewModel.insertFolder(it) },
        onDeleteMemo = { viewModel.deleteMemo(it) },
        onDeleteFolder = { viewModel.deleteFolder(it) },
        onMoveMemo = { memo, targetFolderId -> viewModel.moveMemo(memo, targetFolderId) },
        onMoveFolder = { folder, targetFolderId -> viewModel.moveFolder(folder, targetFolderId) },
        onUpdateFolder = { folder, name -> viewModel.updateFolder(folder, name) },
        onExportMemo = { viewModel.exportMemo(it) },
        onExportFolder = { viewModel.exportFolder(it) },
        onReorder = { from, to -> viewModel.reorder(from, to) }
    )
}

@Composable
fun MemoListScreenContent(
    displayItems: List<MemoListItem>,
    folders: List<Folder>,
    currentFolderPath: String?,
    isCardView: Boolean,
    isSortMode: Boolean,
    onMemoClick: (Memo) -> Unit,
    onSelectFolder: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onAddMemoClick: () -> Unit,
    onAddFolder: (String) -> Unit,
    onDeleteMemo: (Memo) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    onMoveMemo: (Memo, Int?) -> Unit,
    onMoveFolder: (Folder, Int?) -> Unit,
    onUpdateFolder: (Folder, String) -> Unit,
    onExportMemo: (Memo) -> Unit,
    onExportFolder: (Folder) -> Unit,
    onReorder: (Int, Int) -> Unit
) {
    var isShownAddFolderDialog by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    var movingMemo by remember { mutableStateOf<Memo?>(null) }
    var movingFolder by remember { mutableStateOf<Folder?>(null) }
    var editingFolder by remember { mutableStateOf<Folder?>(null) }

    val listState = rememberLazyListState()
    val appBarController = LocalAppBarController.current

    val currentItems by rememberUpdatedState(displayItems)
    val currentOnReorder by rememberUpdatedState(onReorder)
    var localItems by remember { mutableStateOf(displayItems) }
    var draggedItemKey by remember { mutableStateOf<Any?>(null) }
    var targetSwapKey by remember { mutableStateOf<Any?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var startIndex by remember { mutableIntStateOf(-1) }

    var lastSyncedDisplayItems by remember { mutableStateOf(displayItems) }

    LaunchedEffect(displayItems) {
        if (draggedItemKey == null) {
            localItems = displayItems
            lastSyncedDisplayItems = displayItems
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.canScrollForward || listState.canScrollBackward }
            .collect { canScroll ->
                if (!canScroll) {
                    appBarController.setBottomBarVisible(true)
                }
            }
    }

    LaunchedEffect(draggedItemKey) {
        if (draggedItemKey == null) return@LaunchedEffect
        while (true) {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val draggedItem = visibleItems.find { itemInfo -> itemInfo.key == draggedItemKey }
            if (draggedItem != null) {
                val currentPos = draggedItem.offset + dragOffset
                val viewportHeight = layoutInfo.viewportEndOffset
                val threshold = 150f
                val scrollAmount = when {
                    currentPos < threshold -> -15f
                    currentPos > viewportHeight - threshold -> 15f
                    else -> 0f
                }
                if (scrollAmount != 0f) {
                    val consumed = listState.scrollBy(scrollAmount)
                    dragOffset += consumed
                }
            }
            delay(16.milliseconds)
        }
    }

    val isBottomBarVisible = LocalBottomBarVisibility.current

    val fabTranslationY by animateDpAsState(
        targetValue = if (isBottomBarVisible) (-80).dp else 0.dp,
        label = "fabTranslationY"
    )

    val contentPadding = if (isCardView) {
        PaddingValues(
            start = Dimensions.PADDING_MEDIUM,
            top = Dimensions.PADDING_MEDIUM,
            end = Dimensions.PADDING_MEDIUM,
            bottom = Dimensions.LIST_BOTTOM_WITH_FAB
        )
    } else {
        PaddingValues(bottom = Dimensions.LIST_BOTTOM_WITH_FAB)
    }

    val verticalArrangement = if (isCardView) {
        Arrangement.spacedBy(Dimensions.SPACING_MEDIUM)
    } else {
        Arrangement.Top
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (currentFolderPath != null) {
                CurrentFolderHeader(
                    currentFolderPath = currentFolderPath,
                    onNavigateBack = onNavigateBack
                )
            }

            if (localItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.memo_list_empty_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = contentPadding,
                    verticalArrangement = verticalArrangement
                ) {
                    itemsIndexed(
                        items = localItems,
                        key = { _, item ->
                            when (item) {
                                is MemoListItem.FolderItem -> "f_${item.folder.id}"
                                is MemoListItem.MemoItem -> "m_${item.memo.id}"
                            }
                        }
                    ) { _, item ->
                        val itemKey = when (item) {
                            is MemoListItem.FolderItem -> "f_${item.folder.id}"
                            is MemoListItem.MemoItem -> "m_${item.memo.id}"
                        }
                        val isDragging = draggedItemKey == itemKey
                        val scale by animateFloatAsState(
                            targetValue = if (isDragging) 1.05f else 1.0f,
                            label = "scale"
                        )
                        val elevation by animateDpAsState(
                            targetValue = if (isDragging) 8.dp else 0.dp,
                            label = "elevation"
                        )
                        val zIndex = if (isDragging) 10f else 0f

                        val dragModifier = if (isSortMode) {
                            Modifier.pointerInput(itemKey) {
                                detectDragGestures(
                                    onDragStart = {
                                        val currentList = localItems
                                        draggedItemKey = itemKey
                                        dragOffset = 0f
                                        startIndex = currentList.indexOfFirst {
                                            val key = when (it) {
                                                is MemoListItem.FolderItem -> "f_${it.folder.id}"
                                                is MemoListItem.MemoItem -> "m_${it.memo.id}"
                                            }
                                            key == itemKey
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount.y

                                        val layoutInfo = listState.layoutInfo
                                        val visibleItems = layoutInfo.visibleItemsInfo
                                        val draggedItemInfo =
                                            visibleItems.find { it.key == itemKey }
                                                ?: return@detectDragGestures

                                        val currentList = localItems
                                        val currentIndex = currentList.indexOfFirst {
                                            val key = when (it) {
                                                is MemoListItem.FolderItem -> "f_${it.folder.id}"
                                                is MemoListItem.MemoItem -> "m_${it.memo.id}"
                                            }
                                            key == itemKey
                                        }

                                        val draggedVisualCenter =
                                            draggedItemInfo.offset + draggedItemInfo.size / 2 + dragOffset
                                        val targetItemInfo = visibleItems.find { itemInfo ->
                                            if (itemInfo.key == itemKey) return@find false
                                            val itemTop = itemInfo.offset.toFloat()
                                            val itemBottom = (itemInfo.offset + itemInfo.size).toFloat()
                                            draggedVisualCenter > itemTop && draggedVisualCenter < itemBottom
                                        }

                                        if (targetItemInfo != null) {
                                            val targetIndex = targetItemInfo.index
                                            if (targetIndex != -1 && currentIndex != -1 && targetIndex != currentIndex) {
                                                val targetMidPoint =
                                                    targetItemInfo.offset + targetItemInfo.size / 2

                                                if ((targetIndex > currentIndex && draggedVisualCenter > targetMidPoint) ||
                                                    (targetIndex < currentIndex && draggedVisualCenter < targetMidPoint)
                                                ) {
                                                    val oldOffset = draggedItemInfo.offset.toFloat()
                                                    val newOffset = targetItemInfo.offset.toFloat()

                                                    val newList = localItems.toMutableList()
                                                    val moved = newList.removeAt(currentIndex)
                                                    newList.add(targetIndex, moved)
                                                    localItems = newList

                                                    dragOffset += (oldOffset - newOffset)
                                                    targetSwapKey = null
                                                } else {
                                                    targetSwapKey = targetItemInfo.key
                                                }
                                            }
                                        } else {
                                            targetSwapKey = null
                                        }
                                    },
                                    onDragEnd = {
                                        val currentList = localItems
                                        val finalIndex = currentList.indexOfFirst { listItem ->
                                            val key = when (listItem) {
                                                is MemoListItem.FolderItem -> "f_${listItem.folder.id}"
                                                is MemoListItem.MemoItem -> "m_${listItem.memo.id}"
                                            }
                                            key == draggedItemKey
                                        }
                                        if (startIndex != -1 && finalIndex != -1 && startIndex != finalIndex) {
                                            currentOnReorder(startIndex, finalIndex)
                                        }
                                        draggedItemKey = null

                                        if (lastSyncedDisplayItems != currentItems) {
                                            localItems = currentItems
                                            lastSyncedDisplayItems = currentItems
                                        }

                                        targetSwapKey = null
                                        dragOffset = 0f
                                        startIndex = -1
                                    },
                                    onDragCancel = {
                                        draggedItemKey = null

                                        if (lastSyncedDisplayItems != currentItems) {
                                            localItems = currentItems
                                            lastSyncedDisplayItems = currentItems
                                        }

                                        targetSwapKey = null
                                        dragOffset = 0f
                                        startIndex = -1
                                    }
                                )
                            }
                        } else Modifier

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(zIndex)
                                .animateItem(
                                    placementSpec = if (isDragging) null else spring(),
                                    fadeInSpec = null,
                                    fadeOutSpec = null
                                )
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationY = if (isDragging) dragOffset else 0f
                                    shadowElevation = elevation.toPx()
                                    clip = isDragging
                                }
                        ) {
                            if (isCardView) {
                                when (item) {
                                    is MemoListItem.FolderItem -> {
                                        FolderItemCard(
                                            folder = item.folder,
                                            isSortMode = isSortMode,
                                            onClick = { onSelectFolder(item.folder.id) },
                                            onEdit = { editingFolder = item.folder },
                                            onMove = { movingFolder = item.folder },
                                            onExport = { onExportFolder(item.folder) },
                                            onDelete = { onDeleteFolder(item.folder) },
                                            dragHandle = { handleModifier ->
                                                ItemDragIndicator(handleModifier.then(dragModifier))
                                            }
                                        )
                                    }
                                    is MemoListItem.MemoItem -> {
                                        MemoItemCard(
                                            memo = item.memo,
                                            isSortMode = isSortMode,
                                            onClick = { onMemoClick(item.memo) },
                                            onMove = { movingMemo = item.memo },
                                            onExport = { onExportMemo(item.memo) },
                                            onDelete = { onDeleteMemo(item.memo) },
                                            dragHandle = { handleModifier ->
                                                ItemDragIndicator(handleModifier.then(dragModifier))
                                            }
                                        )
                                    }
                                }
                            } else {
                                Column {
                                    when (item) {
                                        is MemoListItem.FolderItem -> {
                                            FolderListItem(
                                                folder = item.folder,
                                                isSortMode = isSortMode,
                                                onClick = { onSelectFolder(item.folder.id) },
                                                onEdit = { editingFolder = item.folder },
                                                onMove = { movingFolder = item.folder },
                                                onExport = { onExportFolder(item.folder) },
                                                onDelete = { onDeleteFolder(item.folder) },
                                                dragHandle = { handleModifier ->
                                                    ItemDragIndicator(handleModifier.then(dragModifier))
                                                }
                                            )
                                        }
                                        is MemoListItem.MemoItem -> {
                                            MemoListItemView(
                                                memo = item.memo,
                                                isSortMode = isSortMode,
                                                onClick = { onMemoClick(item.memo) },
                                                onMove = { movingMemo = item.memo },
                                                onExport = { onExportMemo(item.memo) },
                                                onDelete = { onDeleteMemo(item.memo) },
                                                dragHandle = { handleModifier ->
                                                    ItemDragIndicator(handleModifier.then(dragModifier))
                                                }
                                            )
                                        }
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isFabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                    .clickable { isFabExpanded = false }
            )
        }

        if (!isSortMode) {
            MemoActionButtons(
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
                onAddNewMemo = {
                    isFabExpanded = false
                    onAddMemoClick()
                }
            )
        }
    }

    if (isShownAddFolderDialog) {
        FolderAddDialog(
            onDismiss = { isShownAddFolderDialog = false },
            onConfirm = { folderName ->
                onAddFolder(folderName)
                isShownAddFolderDialog = false
            }
        )
    }

    val currentEditingFolder = editingFolder
    if (currentEditingFolder != null) {
        FolderAddDialog(
            initialName = currentEditingFolder.name,
            titleResId = R.string.log_list_edit_folder_title,
            onDismiss = { editingFolder = null },
            onConfirm = { newName ->
                onUpdateFolder(currentEditingFolder, newName)
                editingFolder = null
            }
        )
    }

    val currentMovingFolder = movingFolder
    if (currentMovingFolder != null) {
        MoveToFolderDialog(
            allFolders = folders,
            excludeFolderId = currentMovingFolder.id,
            onDismiss = { movingFolder = null },
            onConfirm = { targetFolderId ->
                onMoveFolder(currentMovingFolder, targetFolderId)
                movingFolder = null
            }
        )
    }

    val currentMovingMemo = movingMemo
    if (currentMovingMemo != null) {
        MoveToFolderDialog(
            allFolders = folders,
            onDismiss = { movingMemo = null },
            onConfirm = { targetFolderId ->
                onMoveMemo(currentMovingMemo, targetFolderId)
                movingMemo = null
            }
        )
    }
}



@Composable
fun FolderItemCard(
    folder: Folder,
    isSortMode: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onMove: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    dragHandle: @Composable (Modifier) -> Unit = {}
) {
    ItemCard(
        icon = Icons.Default.Folder,
        onClick = onClick,
        enabled = !isSortMode,
        trailingContent = {
            if (isSortMode) {
                dragHandle(Modifier)
            } else {
                ItemOverflowMenu(
                    onEdit = onEdit,
                    onMove = onMove,
                    onExport = onExport,
                    onDelete = onDelete
                )
            }
        }
    ) {
        Text(
            text = folder.name,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FolderListItem(
    folder: Folder,
    isSortMode: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onMove: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    dragHandle: @Composable (Modifier) -> Unit = {}
) {
    ListItem(
        modifier = Modifier.clickable(enabled = !isSortMode, onClick = onClick),
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        headlineContent = {
            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleMedium
            )
        },
        trailingContent = {
            if (isSortMode) {
                dragHandle(Modifier)
            } else {
                ItemOverflowMenu(
                    onEdit = onEdit,
                    onMove = onMove,
                    onExport = onExport,
                    onDelete = onDelete
                )
            }
        }
    )
}

@Composable
fun MemoItemCard(
    memo: Memo,
    isSortMode: Boolean,
    onClick: () -> Unit,
    onMove: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    dragHandle: @Composable (Modifier) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val dateFormat = remember(locale) { SimpleDateFormat("yyyy/MM/dd HH:mm", locale) }
    val dateString = dateFormat.format(Date(memo.updatedAt))

    ItemCard(
        icon = Icons.AutoMirrored.Outlined.StickyNote2,
        onClick = onClick,
        enabled = !isSortMode,
        trailingContent = {
            if (isSortMode) {
                dragHandle(Modifier)
            } else {
                ItemOverflowMenu(
                    onMove = onMove,
                    onExport = onExport,
                    onDelete = onDelete
                )
            }
        }
    ) {
        Text(
            text = memo.name.ifBlank { "No Title" },
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        if (memo.content.isNotBlank()) {
            Text(
                text = memo.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = dateString,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun MemoListItemView(
    memo: Memo,
    isSortMode: Boolean,
    onClick: () -> Unit,
    onMove: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    dragHandle: @Composable (Modifier) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val dateFormat = remember(locale) { SimpleDateFormat("yyyy/MM/dd HH:mm", locale) }
    val dateString = dateFormat.format(Date(memo.updatedAt))

    ListItem(
        modifier = Modifier.clickable(enabled = !isSortMode, onClick = onClick),
        headlineContent = {
            Text(
                text = memo.name.ifBlank { "No Title" },
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(
                text = dateString,
                style = MaterialTheme.typography.bodySmall
            )
        },
        trailingContent = {
            if (isSortMode) {
                dragHandle(Modifier)
            } else {
                ItemOverflowMenu(
                    onMove = onMove,
                    onExport = onExport,
                    onDelete = onDelete
                )
            }
        }
    )
}

@Composable
fun MemoActionButtons(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddNewFolder: () -> Unit,
    onAddNewMemo: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.memo_list_add_new_memo),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onAddNewMemo,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.NoteAdd,
                            contentDescription = stringResource(R.string.talkback_new_memo_button)
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            containerColor = if (expanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
        ) {
            val rotation by animateFloatAsState(if (expanded) 45f else 0f, label = "fabRotation")
            Icon(
                Icons.Filled.Add,
                contentDescription = if (expanded) stringResource(R.string.log_list_cd_close_menu) else stringResource(R.string.log_list_cd_open_menu),
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MemoListScreenContentPreview() {
    SolveLogJetpackComposeTheme {
        val createdAt = System.currentTimeMillis()
        MemoListScreenContent(
            displayItems = listOf(
                MemoListItem.MemoItem(
                    Memo(
                        id = 1,
                        name = "Memo 1",
                        content = "Sample content line 1",
                        createdAt = createdAt,
                        updatedAt = createdAt,
                        priority = 0
                    )
                ),
                MemoListItem.MemoItem(
                    Memo(
                        id = 2,
                        name = "Memo 2",
                        content = "Sample content line 2",
                        createdAt = createdAt,
                        updatedAt = createdAt,
                        priority = 0
                    )
                )
            ),
            folders = emptyList(),
            currentFolderPath = null,
            isCardView = true,
            isSortMode = false,
            onMemoClick = {},
            onSelectFolder = {},
            onNavigateBack = {},
            onExportMemo = {},
            onExportFolder = {},
            onAddFolder = {},
            onDeleteMemo = {},
            onDeleteFolder = {},
            onMoveMemo = { _, _ -> },
            onMoveFolder = { _, _ -> },
            onUpdateFolder = { _, _ -> },
            onAddMemoClick = {},
            onReorder = { _, _ -> }
        )
    }
}
