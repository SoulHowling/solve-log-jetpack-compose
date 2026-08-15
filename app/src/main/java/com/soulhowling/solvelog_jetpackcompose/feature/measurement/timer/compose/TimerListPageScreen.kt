package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.CustomAppBarEffect
import com.soulhowling.solvelog_jetpackcompose.core.LocalAppBarController
import com.soulhowling.solvelog_jetpackcompose.core.LocalBottomBarVisibility
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.viewmodel.TimerListViewModel
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.ActiveTimer
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerPhase
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerState
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import java.util.Locale

@Composable
fun TimerListPageScreen(
    onNavigateToTimerPageScreen: (Int) -> Unit,
    viewModel: TimerListViewModel = viewModel()
) {
    val timers by viewModel.displayTimers.collectAsStateWithLifecycle()
    val activeTimerId by viewModel.activeTimerId.collectAsStateWithLifecycle()

    CustomAppBarEffect(title = null)

    TimerListPageScreenContent(
        timers = timers,
        activeTimerId = activeTimerId,
        onAddTimer = { title, sets, workMin, workSec, breakMin, breakSec ->
            viewModel.insertTimer(title, sets, workMin, workSec, breakMin, breakSec)
        },
        onUpdateTimer = { timerState, title, sets, workMin, workSec, breakMin, breakSec ->
            viewModel.updateTimer(
                timerState = timerState,
                title = title,
                sets = sets,
                workMinutes = workMin,
                workSeconds = workSec,
                breakMinutes = breakMin,
                breakSeconds = breakSec,
            )
        },
        onDeleteTimer = { viewModel.deleteTimer(it) },
        onClicked = { onNavigateToTimerPageScreen(it) },
    )
}

@Composable
fun TimerListPageScreenContent(
    timers: List<ActiveTimer>,
    activeTimerId: Int?,
    onAddTimer: (String, Int, Int, Int, Int, Int) -> Unit,
    onUpdateTimer: (TimerState, String, Int, Int, Int, Int, Int) -> Unit,
    onDeleteTimer: (Int) -> Unit,
    onClicked: (Int) -> Unit,
) {
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

    var showDialog by remember { mutableStateOf(false) }
    var timerIdToStartAfterConfirm by remember { mutableStateOf<Int?>(null) }

    val isBottomBarVisible = LocalBottomBarVisibility.current

    val fabTranslationY by animateDpAsState(
        targetValue = if (isBottomBarVisible) (-80).dp else 0.dp,
        label = "fabTranslationY"
    )

    var editingTimer by remember { mutableStateOf<TimerState?>(null) }
    val isAnyTimerActive = activeTimerId != null

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimensions.PADDING_MEDIUM,
                top = Dimensions.PADDING_MEDIUM,
                end = Dimensions.PADDING_MEDIUM,
                bottom = Dimensions.LIST_BOTTOM_WITH_FAB
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING_MEDIUM)
        ) {
            items(
                items = timers,
                key = { it.id }
            ) { timer ->
                TimerItemCard(
                    modifier = Modifier.animateItem(
                        fadeOutSpec = tween(durationMillis = 300),
                        placementSpec = tween(durationMillis = 300)
                    ),
                    timer = timer,
                    isAnyTimerActive = isAnyTimerActive,
                    activeTimerId = activeTimerId,
                    onEdit = { editingTimer = timer.state },
                    onDelete = { onDeleteTimer(timer.id) },
                    onClicked = {
                        if (isAnyTimerActive && timer.id != activeTimerId) {
                            timerIdToStartAfterConfirm = timer.id
                        } else {
                            onClicked(timer.id)
                        }
                    }
                )
            }
        }

        timerIdToStartAfterConfirm?.let { newTimerId ->
            AlertDialog(
                onDismissRequest = { timerIdToStartAfterConfirm = null },
                title = { Text(stringResource(R.string.timer_dialog_overwrite_title)) },
                text = { Text(stringResource(R.string.timer_dialog_overwrite_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onClicked(newTimerId)
                            timerIdToStartAfterConfirm = null
                        }
                    ) {
                        Text(stringResource(R.string.common_dialog_ok_button))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { timerIdToStartAfterConfirm = null }) {
                        Text(stringResource(R.string.common_dialog_cancel_button))
                    }
                }
            )
        }

        editingTimer?.let { timer ->
            TimerAddDialog(
                initialTimer = timer,
                onDismiss = { editingTimer = null },
                onConfirm = { title, sets, workMinutes, workSeconds, breakMinutes, breakSeconds ->
                    onUpdateTimer(
                        timer,
                        title,
                        sets,
                        workMinutes,
                        workSeconds,
                        breakMinutes,
                        breakSeconds
                    )
                    editingTimer = null
                }
            )
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .graphicsLayer {
                    this.translationY = fabTranslationY.toPx()
                }
                .padding(Dimensions.PADDING_MEDIUM),
            onClick = {
                showDialog = true
            }
        ) {
            Icon(Icons.Filled.Add, "Floating Action Button")
        }

        if (showDialog) {
            TimerAddDialog(
                onDismiss = { showDialog = false },
                onConfirm = { title, sets, workMin, workSec, breakMin, breakSec ->
                    onAddTimer(title, sets, workMin, workSec, breakMin, breakSec)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun TimerItemCard(
    modifier: Modifier = Modifier,
    timer: ActiveTimer,
    isAnyTimerActive: Boolean,
    activeTimerId: Int?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClicked: () -> Unit,
) {
    val breakSet = timer.workSet - 1

    val isCurrentlyManaged = timer.id == activeTimerId || timer.isRunning

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = if (isAnyTimerActive && !isCurrentlyManaged) 0.5f else 1f
            }
            .clickable(
                onClick = onClicked
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyManaged) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(Dimensions.PADDING_MEDIUM)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListItem(
                    modifier = Modifier.fillMaxSize(),
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                        headlineColor = if (isCurrentlyManaged) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        supportingColor = if (isCurrentlyManaged) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    headlineContent = {
                        Text(
                            text = timer.title.ifEmpty { stringResource(R.string.measurement_timer_title) },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    supportingContent = {
                        if (timer.isRunning) {
                            Text(
                                text = stringResource(R.string.timer_list_running),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    },
                    trailingContent = {
                        if (!isCurrentlyManaged) {
                            OverFlowMenu(
                                onEdit = onEdit,
                                onDelete = onDelete
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.SPACING_SMALL_MEDIUM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SPACING_SMALL_MEDIUM)
            ) {
                InfoBlock(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.timer_list_work_time),
                    time = String.format(
                        Locale.ROOT,
                        "%2d:%02d",
                        timer.workSeconds / 60,
                        timer.workSeconds % 60
                    ),
                    subText = stringResource(R.string.timer_list_sets_display, timer.workSet),
                    containerColor = if (isCurrentlyManaged) MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.1f
                    )
                    else MaterialTheme.colorScheme.surface,
                    contentColor = if (isCurrentlyManaged) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )

                val hasBreak = timer.workSet > 1 && timer.breakSeconds > 0
                InfoBlock(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.timer_list_break_time),
                    time = if (hasBreak) {
                        String.format(
                            Locale.ROOT,
                            "%2d:%02d",
                            timer.breakSeconds / 60,
                            timer.breakSeconds % 60
                        )
                    } else {
                        stringResource(R.string.timer_list_none)
                    },
                    subText = if (hasBreak) stringResource(
                        R.string.timer_list_sets_display,
                        breakSet
                    ) else null,
                    containerColor = if (isCurrentlyManaged) MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.1f
                    )
                    else MaterialTheme.colorScheme.surface,
                    contentColor = if (isCurrentlyManaged) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun InfoBlock(
    modifier: Modifier = Modifier,
    label: String,
    time: String,
    subText: String? = null,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.SPACING_SMALL_MEDIUM),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = time,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            subText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            } ?: run {
                Spacer(modifier = Modifier.height(13.dp))
            }
        }
    }
}

@Composable
private fun OverFlowMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { isExpanded = true }
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More option"
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
                    )
                }
            )
        }
    }
}

@Composable
fun TimerAddDialog(
    initialTimer: TimerState? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, sets: Int, workMin: Int, workSec: Int, breakMin: Int, breakSec: Int) -> Unit
) {
    val title = rememberSaveable { mutableStateOf(initialTimer?.title ?: "") }
    val sets = rememberSaveable { mutableIntStateOf(initialTimer?.workSet ?: 4) }
    val workMinutes =
        rememberSaveable { mutableIntStateOf(initialTimer?.workSeconds?.let { it / 60 } ?: 25) }
    val workSeconds =
        rememberSaveable { mutableIntStateOf(initialTimer?.workSeconds?.let { it % 60 } ?: 0) }
    val breakMinutes =
        rememberSaveable { mutableIntStateOf(initialTimer?.breakSeconds?.let { it / 60 } ?: 5) }
    val breakSeconds =
        rememberSaveable { mutableIntStateOf(initialTimer?.breakSeconds?.let { it % 60 } ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (initialTimer == null) R.string.timer_dialog_add_title
                    else R.string.timer_dialog_edit_title
                )
            )
        },
        text = {
            TimerAlertDialogContent(
                title = title,
                sets = sets,
                workMinutes = workMinutes,
                workSeconds = workSeconds,
                breakMinutes = breakMinutes,
                breakSeconds = breakSeconds
            )
        },
        confirmButton = {
            TextButton(
                enabled = sets.intValue > 0 && (workMinutes.intValue > 0 || workSeconds.intValue > 0),
                onClick = {
                    onConfirm(
                        title.value,
                        sets.intValue,
                        workMinutes.intValue,
                        workSeconds.intValue,
                        breakMinutes.intValue,
                        breakSeconds.intValue
                    )
                }
            ) {
                Text(
                    stringResource(
                        if (initialTimer == null) R.string.common_dialog_add_button
                        else R.string.common_dialog_update_button
                    )
                )
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
fun TimerAlertDialogContent(
    title: MutableState<String>,
    sets: MutableIntState,
    workMinutes: MutableIntState,
    workSeconds: MutableIntState,
    breakMinutes: MutableIntState,
    breakSeconds: MutableIntState
) {
    val isEnabledBreakTime = sets.intValue > 1

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING_SMALL_MEDIUM),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            stringResource(R.string.timer_dialog_label_name),
            style = MaterialTheme.typography.titleSmall
        )
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            stringResource(R.string.timer_dialog_label_sets),
            style = MaterialTheme.typography.titleSmall
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberPicker(
                value = sets.intValue,
                range = 1..20,
                onValueChange = { sets.intValue = it },
                modifier = Modifier.weight(1f)
            )
            Text(
                stringResource(R.string.timer_dialog_unit_sets),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Text(
            stringResource(R.string.timer_dialog_label_work),
            style = MaterialTheme.typography.titleSmall
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberPicker(
                value = workMinutes.intValue,
                range = 0..99,
                onValueChange = { workMinutes.intValue = it },
                modifier = Modifier.weight(1f)
            )
            Text(
                stringResource(R.string.timer_dialog_unit_minutes),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            NumberPicker(
                value = workSeconds.intValue,
                range = 0..59,
                onValueChange = { workSeconds.intValue = it },
                modifier = Modifier.weight(1f)
            )
            Text(
                stringResource(R.string.timer_dialog_unit_seconds),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Text(
            stringResource(R.string.timer_dialog_label_break),
            style = MaterialTheme.typography.titleSmall,
            color = if (isEnabledBreakTime) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberPicker(
                value = breakMinutes.intValue,
                range = 0..99,
                onValueChange = { breakMinutes.intValue = it },
                enabled = isEnabledBreakTime,
                modifier = Modifier.weight(1f)
            )
            Text(
                stringResource(R.string.timer_dialog_unit_minutes),
                modifier = Modifier.padding(horizontal = 4.dp),
                color = if (isEnabledBreakTime) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            NumberPicker(
                value = breakSeconds.intValue,
                range = 0..59,
                onValueChange = { breakSeconds.intValue = it },
                enabled = isEnabledBreakTime,
                modifier = Modifier.weight(1f)
            )
            Text(
                stringResource(R.string.timer_dialog_unit_seconds),
                modifier = Modifier.padding(start = 4.dp),
                color = if (isEnabledBreakTime) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}

@Composable
fun NumberPicker(
    value: Int,
    range: Iterable<Int>,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val numbers = remember(range) { range.toList() }
    val pagerState = rememberPagerState(
        initialPage = numbers.indexOf(value).coerceAtLeast(0),
        pageCount = { numbers.size }
    )

    LaunchedEffect(value, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            val targetPage = numbers.indexOf(value).coerceAtLeast(0)
            if (pagerState.currentPage != targetPage) {
                pagerState.scrollToPage(targetPage)
            }
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        val newValue = numbers[pagerState.settledPage]
        if (newValue != value) {
            onValueChange(newValue)
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier.height(100.dp),
        contentPadding = PaddingValues(vertical = 30.dp),
        userScrollEnabled = enabled
    ) { page ->
        val isSelected = pagerState.currentPage == page
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val baseColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }
            Text(
                text = String.format(Locale.ROOT, "%02d", numbers[page]),
                style = if (isSelected) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                color = if (enabled) baseColor else baseColor.copy(alpha = 0.38f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimerListPageScreenContentPreview() {
    SolveLogJetpackComposeTheme {
        TimerListPageScreenContent(
            timers = listOf(
                ActiveTimer(
                    state = TimerState(
                        id = 0,
                        title = "テスト",
                        workSet = 4,
                        workSeconds = 1500,
                        breakSeconds = 300,
                        phase = TimerPhase.WORK,
                    ),
                    initialSeconds = 1500,
                    remainingSeconds = 1500,
                    isRunning = false
                ),
                ActiveTimer(
                    state = TimerState(
                        id = 1,
                        title = "テスト",
                        workSet = 4,
                        workSeconds = 1500,
                        breakSeconds = 300,
                        phase = TimerPhase.BREAK,
                    ),
                    initialSeconds = 300,
                    remainingSeconds = 120,
                    isRunning = true
                )
            ),
            activeTimerId = 1,
            onAddTimer = { _, _, _, _, _, _ -> },
            onUpdateTimer = { _, _, _, _, _, _, _ -> },
            onDeleteTimer = {},
            onClicked = {}
        )
    }
}
