package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils
import java.util.Locale

@Composable
fun TimerPageScreen(
    viewModel: TimerViewModel = viewModel()
) {
    val timers by viewModel.displayTimers.collectAsStateWithLifecycle()

    TimerPageContent(
        timers = timers,
        onStart = { viewModel.start(it) },
        onPause = { viewModel.pause(it)},
        onStop = { viewModel.stop(it) },
        onReset = { viewModel.reset(it) },
        onAddTimer = { minutes, seconds -> viewModel.insertTimer(minutes, seconds) },
        onDeleteTimer = { viewModel.deleteTimer(it) }
    )
}

@Composable
fun TimerPageContent(
    timers: List<TimerState>,
    onStart: (Int) -> Unit,
    onPause: (Int) -> Unit,
    onStop: (Int) -> Unit,
    onReset: (Int) -> Unit,
    onAddTimer: (Int, Int) -> Unit,
    onDeleteTimer: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimensions.PADDING_MEDIUM,
                top = Dimensions.PADDING_MEDIUM,
                end = Dimensions.PADDING_MEDIUM,
                bottom = Dimensions.LIST_BOTTOM_WITH_FAB
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING_MEDIUM)
        ) {
            items(timers) { timer ->
                TimerItemCard(
                    timer = timer,
                    onStart = { onStart(timer.id) },
                    onPause = { onPause(timer.id)},
                    onStop = { onStop(timer.id) },
                    onReset = { onReset(timer.id) },
                    onDelete = { onDeleteTimer(timer.id) }
                )
            }
        }

        NewTimerButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Dimensions.PADDING_MEDIUM),
            onClick = {
                showDialog = true
            }
        )

        if (showDialog) {
            TimerAddDialog(
                onDismiss = { showDialog = false },
                onConfirm = { minutes, seconds ->
                    onAddTimer(minutes, seconds)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun TimerItemCard(
    timer: TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.SPACING_SMALL),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(Dimensions.PADDING_MEDIUM)) {
            Text(
                text = String.format(
                    Locale.ROOT,
                    "%02d:%02d",
                    timer.remainingSeconds / 60,
                    timer.remainingSeconds % 60
                ),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(Dimensions.SPACING_SMALL_MEDIUM))

            LinearProgressIndicator(
                progress = {
                    if (timer.hasTimeLeft()) {
                        timer.remainingSeconds.toFloat() / timer.initialSeconds.toFloat()
                    } else {
                        0f
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(Dimensions.SPACING_SMALL_MEDIUM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToggleIconButton(
                    timer = timer,
                    onStart = onStart,
                    onPause = onPause,
                    onStop = onStop
                )

                IconButton(
                    onClick = {
                        LogUtils.d("タイマーリセット: ${timer.remainingSeconds} 秒")
                        onReset()
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "リセット")
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = {
                        LogUtils.d("タイマー削除")
                        onDelete()
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "削除")
                }
            }
        }
    }
}

@Composable
fun NewTimerButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },
        modifier = modifier
    ) {
        Icon(Icons.Filled.Add, "Floating Action Button")
    }
}

@Composable
fun ToggleIconButton(
    timer: TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    if (timer.isFinished()) {
        IconButton(
            onClick = {
                LogUtils.d("タイマー: 停止（リセット）")
                onStop()
            }
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "停止",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        IconButton(
            onClick = {
                val isTicking = timer.isTicking()
                LogUtils.d("タイマー: ${if (isTicking) "一時停止" else "開始"}")
                if (isTicking) {
                    onPause()
                } else {
                    onStart()
                }
            }
        ) {
            Icon(
                imageVector = if (timer.isTicking()) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (timer.isTicking()) "一時停止" else "開始",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TimerAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int, seconds: Int) -> Unit
) {
    var minutes by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.timer_add_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING_SMALL_MEDIUM),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.timer_add_description),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberPicker(
                        value = minutes,
                        range = 0..99,
                        onValueChange = { minutes = it },
                        modifier = Modifier.weight(1f)
                    )
                    Text("分", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(Dimensions.SPACING_MEDIUM))
                    NumberPicker(
                        value = seconds,
                        range = 0..59,
                        onValueChange = { seconds = it },
                        modifier = Modifier.weight(1f)
                    )
                    Text("秒", style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = (minutes > 0 || seconds > 0),
                onClick = {
                    onConfirm(minutes, seconds)
                }
            ) {
                Text(stringResource(R.string.timer_add_confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.timer_add_cancel_button))
            }
        }
    )
}

@Composable
fun NumberPicker(
    value: Int,
    range: Iterable<Int>,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val numbers = remember(range) { range.toList() }
    val pagerState = rememberPagerState(
        initialPage = numbers.indexOf(value).coerceAtLeast(0),
        pageCount = { numbers.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        onValueChange(numbers[pagerState.currentPage])
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier.height(150.dp),
        contentPadding = PaddingValues(vertical = 50.dp)
    ) { page ->
        val isSelected = pagerState.currentPage == page
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format(Locale.ROOT, "%02d", numbers[page]),
                style = if (isSelected) {
                    MaterialTheme.typography.displayMedium
                } else {
                    MaterialTheme.typography.titleMedium
                },
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimerPageScreenPreview() {
    SolveLogJetpackComposeTheme {
        TimerPageContent(
            timers = listOf(
                TimerState(0, 60, 60, false),
                TimerState(1, 180, 120, true),
                TimerState(2, 100, 0, true)
            ),
            onStart = {},
            onPause = {},
            onStop = {},
            onReset = {},
            onAddTimer = { _, _ -> },
            onDeleteTimer = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ToggleIconButtonPreview() {
    SolveLogJetpackComposeTheme {
        ToggleIconButton(
            timer = TimerState(
                id = 0,
                initialSeconds = 60,
                remainingSeconds = 60,
                isRunning = true
            ),
            onStart = {},
            onPause = {},
            onStop = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
fun TimerAddDialogPreview() {
    SolveLogJetpackComposeTheme {
        TimerAddDialog(
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}