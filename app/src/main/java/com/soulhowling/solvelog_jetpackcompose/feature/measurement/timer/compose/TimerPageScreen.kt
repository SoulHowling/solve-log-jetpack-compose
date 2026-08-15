package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.CustomAppBarEffect
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.ControlButtons
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import java.util.Locale

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.ActiveTimer
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerPhase
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerState
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.viewmodel.TimerViewModel
import com.soulhowling.solvelog_jetpackcompose.utils.rememberNotificationPermissionHandler

@Composable
fun TimerPageScreen(
    viewModel: TimerViewModel = viewModel()
) {
    CustomAppBarEffect(title = stringResource(R.string.measurement_timer_title))
    val timer by viewModel.displayedTimer.collectAsState()
    val totalSets by viewModel.totalSets.collectAsState()
    val isVertical = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    timer?.let {
        TimerPageScreenContent(
            timer = it,
            totalSets = totalSets,
            onStart = { viewModel.toggleTimer() },
            onReset = { viewModel.resetTimer() },
            onRestart = { viewModel.restartTimer() },
            isVertical = isVertical
        )
    }
}


@Composable
fun TimerPageScreenContent(
    timer: ActiveTimer,
    totalSets: Int,
    onStart: () -> Unit,
    onReset: () -> Unit,
    onRestart: () -> Unit,
    isVertical: Boolean,
) {
    val onStartClick = rememberNotificationPermissionHandler(onPermissionGranted = onStart)
    val onRestartClick = rememberNotificationPermissionHandler(onPermissionGranted = onRestart)
    
    val isRunning = timer.isRunning
    val isPaused = !timer.isRunning && timer.remainingSeconds < timer.initialSeconds

    var backgroundColor: Color
    var foregroundColor: Color
    var phaseText: String
    var isCompleted = false
    when (timer.phase) {
        TimerPhase.WORK -> {
            backgroundColor = MaterialTheme.colorScheme.surface
            foregroundColor = MaterialTheme.colorScheme.onSurface
            phaseText = stringResource(R.string.timer_page_phase_work)
        }

        TimerPhase.BREAK -> {
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
            foregroundColor = MaterialTheme.colorScheme.onSecondaryContainer
            phaseText = stringResource(R.string.timer_page_phase_break)
        }

        TimerPhase.COMPLETE -> {
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
            foregroundColor = MaterialTheme.colorScheme.onPrimaryContainer
            phaseText = stringResource(R.string.timer_page_phase_complete)
            isCompleted = true
        }
    }

    val currentSetDisplay = if (timer.phase == TimerPhase.WORK) {
        (totalSets - timer.workSet + 1).coerceAtLeast(1)
    } else {
        (totalSets - timer.workSet).coerceAtLeast(1)
    }
    val setsText = stringResource(R.string.timer_page_work_set_format, currentSetDisplay, totalSets)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        if (isVertical) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp, bottom = 80.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timer.title,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = {
                            if (timer.initialSeconds > 0) {
                                1f - (timer.remainingSeconds.toFloat() / timer.initialSeconds.toFloat())
                            } else 0f
                        },
                        modifier = Modifier.fillMaxSize(),
                        color = foregroundColor,
                        strokeWidth = 8.dp,
                        trackColor = foregroundColor.copy(alpha = 0.2f),
                        strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                    )
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = phaseText,
                            style = MaterialTheme.typography.headlineMedium,
                            color = foregroundColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = String.format(
                                Locale.ROOT,
                                "%02d:%02d",
                                timer.remainingSeconds / 60,
                                timer.remainingSeconds % 60
                            ),
                            style = MaterialTheme.typography.displayLarge,
                            color = foregroundColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = setsText,
                            style = MaterialTheme.typography.headlineSmall,
                            color = foregroundColor,
                            textAlign = TextAlign.Center
                        )
                    }

                }

                Spacer(modifier = Modifier.height(Dimensions.SPACING_LARGE))
                ControlButtons(
                    isRunning = isRunning,
                    isPaused = isPaused,
                    isCompleted = isCompleted,
                    showReset = true,
                    onStartClick = if (isCompleted) onRestartClick else onStartClick,
                    onReset = onReset,
                    isVertical = true,
                )

            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 80.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timer.title,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.PADDING_LARGE)
                        .height(8.dp),
                    color = foregroundColor,
                    trackColor = foregroundColor.copy(alpha = 0.2f),
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    progress = {
                        if (timer.initialSeconds > 0) {
                            1f - (timer.remainingSeconds.toFloat() / timer.initialSeconds.toFloat())
                        } else 0f
                    },
                )
                Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM_LARGE))
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = phaseText,
                            style = MaterialTheme.typography.headlineMedium,
                            color = foregroundColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = setsText,
                            style = MaterialTheme.typography.headlineSmall,
                            color = foregroundColor,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.width(Dimensions.SPACING_LARGE))
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format(
                                Locale.ROOT,
                                "%02d:%02d",
                                timer.remainingSeconds / 60,
                                timer.remainingSeconds % 60
                            ),
                            style = MaterialTheme.typography.displayLarge,
                            color = foregroundColor,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.width(Dimensions.SPACING_LARGE))
                    ControlButtons(
                        isRunning = isRunning,
                        isPaused = isPaused,
                        isCompleted = isCompleted,
                        showReset = true,
                        onStartClick = if (isCompleted) onRestartClick else onStartClick,
                        onReset = onReset,
                        isVertical = false,
                    )
                }
            }

        }
    }
}


@Preview(showBackground = true)
@Composable
fun TimerPageScreenVerticalContentPreview() {
    SolveLogJetpackComposeTheme {
        TimerPageScreenContent(
            timer = ActiveTimer(
                state = TimerState(
                    id = 1,
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
            totalSets = 4,
            onStart = {},
            onReset = {},
            onRestart = {},
            isVertical = true,
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
fun TimerPageScreenHorizontalContentPreview() {
    SolveLogJetpackComposeTheme {
        TimerPageScreenContent(
            timer = ActiveTimer(
                state = TimerState(
                    id = 1,
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
            totalSets = 4,
            onStart = {},
            onReset = {},
            onRestart = {},
            isVertical = false,
        )
    }
}
