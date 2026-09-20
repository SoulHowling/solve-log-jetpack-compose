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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.scrollBy
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.CustomAppBarEffect
import com.soulhowling.solvelog_jetpackcompose.core.LocalAppBarController
import com.soulhowling.solvelog_jetpackcompose.core.LocalBottomBarVisibility
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.AnswerFormat
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.ViewType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel.ExportTarget
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel.SolveLogListItem
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel.SolveLogListViewModel
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import kotlin.time.Duration.Companion.milliseconds

object SolveLogListTestTags {
    const val TOGGLE_VIEW_MODE = "toggle_view_mode"
    const val MORE_OPTIONS = "more_options"
    const val ADD_FAB = "add_fab"
    const val ADD_FOLDER_FAB = "add_folder_fab"
    const val ADD_LOG_FAB = "add_log_fab"
    const val FOLDER_ITEM = "folder_item_"
    const val LOG_ITEM = "log_item_"
    const val BACK_BUTTON = "back_button"
    const val SORT_OK_BUTTON = "sort_ok_button"
    const val EXPORT_ALL_MENU_ITEM = "export_all_menu_item"
    const val IMPORT_MENU_ITEM = "import_menu_item"
    const val SORT_MENU_ITEM = "sort_menu_item"
}

@Composable
fun SolveLogListScreen(
    onNavigateToViewer: (Int) -> Unit,
    viewModel: SolveLogListViewModel = viewModel()
) {
    val displayItems by viewModel.displayItems.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val currentFolderPath by viewModel.currentFolderPath.collectAsStateWithLifecycle()
    val viewType by viewModel.viewType.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingExportTarget by rememberSaveable { mutableStateOf<ExportTarget?>(null) }
    val exportDocumentLauncher = rememberLauncherForActivityResult(
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

    val importDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.performImport(uri) { success ->
                if (success) {
                    Toast.makeText(context, R.string.common_import_success, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(context, R.string.common_import_failed, Toast.LENGTH_SHORT)
                        .show()
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

    val deleteLogSuccessFormat = stringResource(R.string.log_list_delete_log_success)
    val deleteFolderSuccessFormat = stringResource(R.string.log_list_delete_folder_success)
    val moveSuccessMessage = stringResource(R.string.log_list_move_success)
    var isExpanded by remember { mutableStateOf(false) }
    var isSortMode by remember { mutableStateOf(false) }

    CustomAppBarEffect(
        title = if (isSortMode) stringResource(R.string.log_list_dropdown_menu_item_sort) else null,
        actions = {
            if (isSortMode) {
                TextButton(
                    onClick = { isSortMode = false },
                    modifier = Modifier.testTag(SolveLogListTestTags.SORT_OK_BUTTON)
                ) {
                    Text(
                        text = stringResource(R.string.common_dialog_ok_button),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                IconButton(
                    onClick = { viewModel.toggleViewMode() },
                    modifier = Modifier.testTag(SolveLogListTestTags.TOGGLE_VIEW_MODE)
                ) {
                    Icon(
                        imageVector = viewType.nextButtonImage,
                        contentDescription = stringResource(R.string.log_list_cd_toggle_view),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(
                    onClick = { isExpanded = true },
                    modifier = Modifier.testTag(SolveLogListTestTags.MORE_OPTIONS)
                ) {
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
                        modifier = Modifier.testTag(SolveLogListTestTags.EXPORT_ALL_MENU_ITEM),
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
                    DropdownMenuItem(
                        modifier = Modifier.testTag(SolveLogListTestTags.IMPORT_MENU_ITEM),
                        onClick = {
                            importDocumentLauncher.launch(arrayOf("application/json"))
                            isExpanded = false
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.log_list_dropdown_menu_item_import),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                            )
                        }
                    )
                    DropdownMenuItem(
                        modifier = Modifier.testTag(SolveLogListTestTags.SORT_MENU_ITEM),
                        onClick = {
                            isExpanded = false
                            isSortMode = true
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.log_list_dropdown_menu_item_sort),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                            )
                        }
                    )
                }
            }
        }
    )

    SolveLogListScreenContent(
        displayItems = displayItems,
        folders = folders,
        currentFolderPath = currentFolderPath,
        isCardView = viewType == ViewType.CARD,
        isSortMode = isSortMode,
        onNavigateToViewer = onNavigateToViewer,
        onSelectFolder = { viewModel.selectFolder(it) },
        onNavigateBack = { viewModel.navigateBack() },
        onAddSolveLog = { name, quantity, answerFormat, selectionType, selectionQuantity ->
            viewModel.insertSolveLog(name, quantity, answerFormat, selectionType, selectionQuantity)
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
        onUpdateSolveLog = { log, name, quantity, answerFormat, selectionType, selectionQuantity ->
            viewModel.updateSolveLog(log, name, quantity, answerFormat, selectionType, selectionQuantity)
        },
        onUpdateFolder = { folder, name ->
            viewModel.updateFolder(folder, name)
        },
        onExportLog = { log ->
            viewModel.export(log)
        },
        onExportFolder = { folder ->
            viewModel.export(folder)
        },
        onReorder = { from, to ->
            viewModel.reorder(from, to)
        }
    )
}

@Composable
internal fun SolveLogListScreenContent(
    displayItems: List<SolveLogListItem>,
    folders: List<Folder>,
    currentFolderPath: String?,
    isCardView: Boolean,
    isSortMode: Boolean,
    onNavigateToViewer: (Int) -> Unit,
    onSelectFolder: (Int?) -> Unit,
    onNavigateBack: () -> Unit,
    onAddSolveLog: (String, Int, AnswerFormat, SelectionType, SelectionQuantity) -> Unit,
    onAddFolder: (String) -> Unit,
    onDeleteSolveLog: (SolveLog) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    onMoveSolveLog: (SolveLog, Int?) -> Unit,
    onMoveFolder: (Folder, Int?) -> Unit,
    onUpdateSolveLog: (SolveLog, String, Int, AnswerFormat, SelectionType, SelectionQuantity) -> Unit,
    onUpdateFolder: (Folder, String) -> Unit,
    onExportLog: (SolveLog) -> Unit,
    onExportFolder: (Folder) -> Unit,
    onReorder: (Int, Int) -> Unit,
) {
    var isShownBottomSheet by remember { mutableStateOf(false) }
    var isShownAddFolderDialog by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val appBarController = LocalAppBarController.current

    val currentDisplayItems by rememberUpdatedState(displayItems)
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
                    // 実際にスクロールできた量（consumed）だけオフセットを調整する
                    val consumed = listState.scrollBy(scrollAmount)
                    dragOffset += consumed
                }
            }
            delay(16.milliseconds)
        }
    }

    var movingLog by remember { mutableStateOf<SolveLog?>(null) }
    var movingFolder by remember { mutableStateOf<Folder?>(null) }
    var editingLog by remember { mutableStateOf<SolveLog?>(null) }
    var editingFolder by remember { mutableStateOf<Folder?>(null) }

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
        Arrangement.spacedBy(0.dp)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (currentFolderPath != null) {
                CurrentFolderHeader(
                    currentFolderPath = currentFolderPath,
                    onNavigateBack = onNavigateBack,
                )
            }

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
                            is SolveLogListItem.FolderItem -> "folder_${item.folder.id}"
                            is SolveLogListItem.LogItem -> "log_${item.log.id}"
                        }
                    }
                ) { _, item ->
                    val itemKey = when (item) {
                        is SolveLogListItem.FolderItem -> "folder_${item.folder.id}"
                        is SolveLogListItem.LogItem -> "log_${item.log.id}"
                    }

                    val isDragging = draggedItemKey == itemKey
                    val isTarget = targetSwapKey == itemKey
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
                                            is SolveLogListItem.FolderItem -> "folder_${it.folder.id}"
                                            is SolveLogListItem.LogItem -> "log_${it.log.id}"
                                        }
                                        key == itemKey
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount.y

                                    val layoutInfo = listState.layoutInfo
                                    val visibleItems = layoutInfo.visibleItemsInfo
                                    val draggedItemInfo = visibleItems.find { it.key == itemKey }
                                        ?: return@detectDragGestures

                                    // 常に最新の localItems の状態に基づいて検索
                                    val currentList = localItems
                                    val currentIndex = currentList.indexOfFirst {
                                        val key = when (it) {
                                            is SolveLogListItem.FolderItem -> "folder_${it.folder.id}"
                                            is SolveLogListItem.LogItem -> "log_${it.log.id}"
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
                                        // targetItemInfo.index はリスト全体でのインデックス
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

                                                // スワップが発生したので指の位置を補正
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
                                            is SolveLogListItem.FolderItem -> "folder_${listItem.folder.id}"
                                            is SolveLogListItem.LogItem -> "log_${listItem.log.id}"
                                        }
                                        key == draggedItemKey
                                    }
                                    if (startIndex != -1 && finalIndex != -1 && startIndex != finalIndex) {
                                        currentOnReorder(startIndex, finalIndex)
                                    }
                                    draggedItemKey = null

                                    // 最新の displayItems を参照して同期チェック
                                    if (lastSyncedDisplayItems != currentDisplayItems) {
                                        localItems = currentDisplayItems
                                        lastSyncedDisplayItems = currentDisplayItems
                                    }

                                    targetSwapKey = null
                                    dragOffset = 0f
                                    startIndex = -1
                                },
                                onDragCancel = {
                                    draggedItemKey = null

                                    // キャンセル時も最新の displayItems で同期
                                    if (lastSyncedDisplayItems != currentDisplayItems) {
                                        localItems = currentDisplayItems
                                        lastSyncedDisplayItems = currentDisplayItems
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
                                shape = RoundedCornerShape(if (isCardView) 12.dp else 0.dp)
                                clip = isDragging
                            }
                    ) {
                        if (isTarget) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = (-Dimensions.SPACING_MEDIUM / 2))
                                    .zIndex(2f),
                                thickness = 4.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (isCardView) {
                            when (item) {
                                is SolveLogListItem.FolderItem -> {
                                    FolderItemCard(
                                        title = item.folder.name,
                                        onClick = { onSelectFolder(item.folder.id) },
                                        onDelete = { onDeleteFolder(item.folder) },
                                        onMove = { movingFolder = item.folder },
                                        onExportFolder = { onExportFolder(item.folder) },
                                        onEdit = { editingFolder = item.folder },
                                        isSortMode = isSortMode,
                                        dragHandle = { modifier ->
                                            DragIndicator(
                                                modifier.then(
                                                    dragModifier
                                                )
                                            )
                                        },
                                        modifier = Modifier.testTag(SolveLogListTestTags.FOLDER_ITEM + item.folder.id)
                                    )
                                }

                                is SolveLogListItem.LogItem -> {
                                    SolveLogItemCard(
                                        title = item.log.name,
                                        questionQuantity = item.log.questionQuantity,
                                        attemptCount = item.log.attemptCount,
                                        answerFormat = item.log.answerFormat,
                                        selectionTypeIndex = item.log.selectionType,
                                        selectionQuantityIndex = item.log.selectionQuantity,
                                        onClick = { onNavigateToViewer(item.log.id) },
                                        onDelete = { onDeleteSolveLog(item.log) },
                                        onMove = { movingLog = item.log },
                                        onExport = { onExportLog(item.log) },
                                        onEdit = { editingLog = item.log },
                                        isSortMode = isSortMode,
                                        dragHandle = { modifier ->
                                            DragIndicator(
                                                modifier.then(
                                                    dragModifier
                                                )
                                            )
                                        },
                                        modifier = Modifier.testTag(SolveLogListTestTags.LOG_ITEM + item.log.id)
                                    )
                                }
                            }
                        } else {
                            when (item) {
                                is SolveLogListItem.FolderItem -> {
                                    SolveListItem(
                                        isLog = false,
                                        title = item.folder.name,
                                        onClick = { onSelectFolder(item.folder.id) },
                                        onDelete = { onDeleteFolder(item.folder) },
                                        onMove = { movingFolder = item.folder },
                                        onExport = { onExportFolder(item.folder) },
                                        onEdit = { editingFolder = item.folder },
                                        isSortMode = isSortMode,
                                        dragHandle = { modifier ->
                                            DragIndicator(
                                                modifier.then(
                                                    dragModifier
                                                )
                                            )
                                        },
                                        modifier = Modifier.testTag(SolveLogListTestTags.FOLDER_ITEM + item.folder.id)
                                    )
                                }

                                is SolveLogListItem.LogItem -> {
                                    SolveListItem(
                                        isLog = true,
                                        title = item.log.name,
                                        onClick = { onNavigateToViewer(item.log.id) },
                                        onDelete = { onDeleteSolveLog(item.log) },
                                        onMove = { movingLog = item.log },
                                        onExport = { onExportLog(item.log) },
                                        onEdit = { editingLog = item.log },
                                        isSortMode = isSortMode,
                                        dragHandle = { modifier ->
                                            DragIndicator(
                                                modifier.then(
                                                    dragModifier
                                                )
                                            )
                                        },
                                        modifier = Modifier.testTag(SolveLogListTestTags.LOG_ITEM + item.log.id)
                                    )
                                }
                            }
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
                .padding(Dimensions.PADDING_MEDIUM)
                .testTag(SolveLogListTestTags.ADD_FAB),
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

        editingFolder?.let { folder ->
            FolderAddDialog(
                initialName = folder.name,
                titleResId = R.string.log_list_edit_folder_title,
                onDismiss = { editingFolder = null },
                onConfirm = { name ->
                    onUpdateFolder(folder, name)
                    editingFolder = null
                }
            )
        }

        if (isShownBottomSheet) {
            SolveLogAddBottomSheet(
                onDismiss = { isShownBottomSheet = false },
                onConfirm = { name, quantity, answerFormat, selectionType, selectionQuantity ->
                    onAddSolveLog(name, quantity, answerFormat, selectionType, selectionQuantity)
                }
            )
        }

        editingLog?.let { log ->
            SolveLogAddBottomSheet(
                initialName = log.name,
                initialQuestionQuantity = log.questionQuantity,
                initialAnswerFormat = AnswerFormat.entries.find { it.index == log.answerFormat } ?: AnswerFormat.MULTIPLE_CHOICE,
                initialSelectionType = SelectionType.entries.getOrElse(log.selectionType) { SelectionType.NUMBER },
                initialSelectionQuantity = SelectionQuantity.entries.getOrElse(log.selectionQuantity) { SelectionQuantity.FOUR },
                titleResId = R.string.log_list_edit_log_title,
                onDismiss = { editingLog = null },
                onConfirm = { name, quantity, answerFormat, selectionType, selectionQuantity ->
                    onUpdateSolveLog(log, name, quantity, answerFormat, selectionType, selectionQuantity)
                    editingLog = null
                }
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
    onEdit: () -> Unit,
    isSortMode: Boolean,
    modifier: Modifier = Modifier,
    dragHandle: @Composable (Modifier) -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isSortMode) { onClick() },
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
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = Dimensions.PADDING_MEDIUM)
                    .size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            if (isSortMode) {
                dragHandle(Modifier)
            } else {
                OverFlowMenu(
                    onDelete = onDelete,
                    onMove = onMove,
                    onExport = onExportFolder,
                    onEdit = onEdit,
                )
            }
        }
    }
}

@Composable
private fun SolveLogItemCard(
    title: String,
    questionQuantity: Int,
    attemptCount: Int,
    answerFormat: Int,
    selectionTypeIndex: Int,
    selectionQuantityIndex: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onExport: () -> Unit,
    onEdit: () -> Unit,
    isSortMode: Boolean,
    modifier: Modifier = Modifier,
    dragHandle: @Composable (Modifier) -> Unit = {},
) {
    val answerFormat = AnswerFormat.from(answerFormat)
    val selectionType = SelectionType.from(selectionTypeIndex)
    val selectionQuantity = SelectionQuantity.from(selectionQuantityIndex)
    val categoryText = when (answerFormat) {
        AnswerFormat.FREE_RESPONSE -> stringResource(
            R.string.log_list_description_free_response
        )
        AnswerFormat.MULTIPLE_CHOICE -> stringResource(
            R.string.log_list_description_selection,
            selectionType.displayString,
            selectionQuantity.displayString
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isSortMode) { onClick() },
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                InfoBadge(
                    icon = Icons.Default.Numbers,
                    text = stringResource(
                        R.string.log_list_description_question_quantity,
                        questionQuantity
                    )
                )
                InfoBadge(
                    icon = Icons.Default.History,
                    text = stringResource(R.string.log_list_description_attempt_count, attemptCount)
                )
                InfoBadge(
                    icon = Icons.Default.Category,
                    text = categoryText
                )
            }
            if (isSortMode) {
                dragHandle(Modifier)
            } else {
                OverFlowMenu(
                    onDelete = onDelete,
                    onMove = onMove,
                    onExport = onExport,
                    onEdit = onEdit,
                )
            }
        }
    }
}

@Composable
fun SolveListItem(
    isLog: Boolean,
    title: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onExport: () -> Unit,
    onEdit: () -> Unit,
    isSortMode: Boolean,
    modifier: Modifier = Modifier,
    dragHandle: @Composable (Modifier) -> Unit = {},
) {
    Column(
        modifier = modifier
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isSortMode) { onClick() },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                headlineColor = MaterialTheme.colorScheme.onSurface,
                leadingIconColor = MaterialTheme.colorScheme.onSurface,
                trailingIconColor = MaterialTheme.colorScheme.onSurface
            ),
            leadingContent = {
                Icon(
                    imageVector = if (isLog) Icons.AutoMirrored.Filled.Assignment else Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            trailingContent = {
                if (isSortMode) {
                    dragHandle(Modifier)
                } else {
                    OverFlowMenu(
                        onDelete = onDelete,
                        onMove = onMove,
                        onExport = onExport,
                        onEdit = onEdit,
                    )
                }
            }
        )
        HorizontalDivider()
    }
}

@Composable
private fun InfoBadge(
    icon: ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OverFlowMenu(
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onExport: () -> Unit,
    onEdit: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { isExpanded = true }
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.log_list_cd_more_options)
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
                    onEdit()
                    isExpanded = false
                },
                text = {
                    Text(
                        text = stringResource(R.string.log_list_dropdown_menu_item_edit),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                    )
                }
            )
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
private fun DragIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription = stringResource(R.string.log_list_cd_drag_handle),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun CurrentFolderHeader(
    currentFolderPath: String,
    onNavigateBack: () -> Unit,
) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.PADDING_MEDIUM, vertical = Dimensions.PADDING_SMALL)
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onNavigateBack() },
            modifier = Modifier.testTag(SolveLogListTestTags.BACK_BUTTON)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.log_list_cd_back)
            )
        }
        Text(
            text = currentFolderPath,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimensions.PADDING_SMALL)
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
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.testTag(SolveLogListTestTags.ADD_FOLDER_FAB)
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
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.testTag(SolveLogListTestTags.ADD_LOG_FAB)
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

@Preview(showBackground = true)
@Composable
private fun SolveLogListScreenContentPreview() {
    val solveLogs = List(10) { index ->
        SolveLog(
            id = index,
            name = "テスト#$index",
            questionQuantity = 30,
            answerFormat = AnswerFormat.MULTIPLE_CHOICE.index,
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
            isCardView = true,
            isSortMode = true,
            onNavigateToViewer = {},
            onSelectFolder = {},
            onNavigateBack = {},
            onAddSolveLog = { _, _, _, _, _ -> },
            onAddFolder = {},
            onDeleteSolveLog = {},
            onDeleteFolder = {},
            onMoveSolveLog = { _, _ -> },
            onMoveFolder = { _, _ -> },
            onUpdateSolveLog = { _, _, _, _, _, _ -> },
            onUpdateFolder = { _, _ -> },
            onExportLog = {},
            onExportFolder = {},
            onReorder = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SolveLogListItemPreview() {
    val sampleLog = SolveLog(
        id = 1,
        name = "サンプルログ",
        questionQuantity = 30,
        answerFormat = AnswerFormat.MULTIPLE_CHOICE.index,
        selectionType = SelectionType.NUMBER.index,
        selectionQuantity = SelectionQuantity.FOUR.index,
        attemptCount = 5,
        createdAt = 0L,
        updatedAt = 0L,
        folderId = null
    )
    SolveLogJetpackComposeTheme {
        Column {
            SolveListItem(
                isLog = true,
                title = sampleLog.name,
                onClick = {},
                onDelete = {},
                onMove = {},
                onExport = {},
                onEdit = {},
                isSortMode = false
            )
            SolveListItem(
                isLog = false,
                title = "サンプルフォルダ",
                onClick = {},
                onDelete = {},
                onMove = {},
                onExport = {},
                onEdit = {},
                isSortMode = false
            )
        }
    }
}