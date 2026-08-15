package com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.ControlButtons
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import com.soulhowling.solvelog_jetpackcompose.utils.rememberNotificationPermissionHandler
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun StopwatchPageScreen(
    viewModel: StopwatchViewModel = viewModel()
) {
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()
    val accumulatedTime by viewModel.accumulatedTime.collectAsStateWithLifecycle()
    val baseTime by viewModel.baseTime.collectAsStateWithLifecycle()

    var displayTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning, baseTime, accumulatedTime) {
        if (isRunning) {
            while (true) {
                displayTime = System.currentTimeMillis() - baseTime
                delay(10.milliseconds)
            }
        } else {
            displayTime = accumulatedTime
        }
    }

    val isVertical = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    StopwatchPageScreenContent(
        displayTime = displayTime,
        isRunning = isRunning,
        isPaused = isPaused,
        isVertical = isVertical,
        onToggleStartStop = viewModel::toggleStartStop,
        onFormatTime = viewModel::formatTime,
        onReset = viewModel::reset
    )
}

@Composable
fun StopwatchPageScreenContent(
    displayTime: Long,
    isRunning: Boolean,
    isPaused: Boolean,
    isVertical: Boolean,
    onToggleStartStop: () -> Unit,
    onFormatTime: (Long) -> String,
    onReset: () -> Unit
) {
    val onStartClick =
        rememberNotificationPermissionHandler(onPermissionGranted = onToggleStartStop)

    Surface(modifier = Modifier.fillMaxSize()) {
        if (isVertical) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TimeDisplay(onFormatTime(displayTime), fontSize = 52.sp)
                Spacer(modifier = Modifier.height(Dimensions.SPACING_LARGE))
                ControlButtons(
                    isRunning = isRunning,
                    isPaused = isPaused,
                    isCompleted = false,
                    showReset = displayTime > 0,
                    onStartClick = onStartClick,
                    onReset = onReset,
                    isVertical = true
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.PADDING_MEDIUM),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        TimeDisplay(onFormatTime(displayTime), fontSize = 48.sp)
                    }
                    Spacer(modifier = Modifier.width(Dimensions.SPACING_LARGE))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ControlButtons(
                            isRunning = isRunning,
                            isPaused = isPaused,
                            isCompleted = false,
                            showReset = displayTime > 0,
                            onStartClick = onStartClick,
                            onReset = onReset,
                            isVertical = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeDisplay(timeText: String, fontSize: TextUnit) {
    Text(
        text = timeText,
        modifier = Modifier.padding(horizontal = Dimensions.PADDING_MEDIUM),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace
        )
    )
}

@Preview(showBackground = true)
@Composable
fun StopWatchPageScreenVerticalContentPreview() {
    SolveLogJetpackComposeTheme {
        StopwatchPageScreenContent(
            displayTime = 0L,
            isRunning = false,
            isPaused = false,
            isVertical = true,
            onToggleStartStop = {},
            onFormatTime = { "00:00:00.00" },
            onReset = {}
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
fun StopWatchPageScreenLandscapePreview() {
    SolveLogJetpackComposeTheme {
        StopwatchPageScreenContent(
            displayTime = 1L,
            isRunning = false,
            isPaused = false,
            isVertical = false,
            onToggleStartStop = {},
            onFormatTime = { "00:00:00.00" },
            onReset = {}
        )
    }
}
