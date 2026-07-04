package com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun StopwatchPageScreen(
    viewModel: StopwatchViewModel = viewModel()
) {
    val context = LocalContext.current
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
        context = context,
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
    context: Context,
    displayTime: Long,
    isRunning: Boolean,
    isPaused: Boolean,
    isVertical: Boolean,
    onToggleStartStop: () -> Unit,
    onFormatTime: (Long) -> String,
    onReset: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        onToggleStartStop()
    }

    val onStartClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (status != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onToggleStartStop()
            }
        } else {
            onToggleStartStop()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (isVertical) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TimeDisplay(onFormatTime(displayTime), fontSize = 56.sp)
                Spacer(modifier = Modifier.height(Dimensions.SPACING_LARGE))
                ControlButtons(
                    isRunning = isRunning,
                    isPaused = isPaused,
                    showReset = displayTime > 0,
                    onStartClick = onStartClick,
                    onReset = onReset,
                    buttonWidth = 120.dp,
                    isVertical = true
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.PADDING_MEDIUM),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
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
                        showReset = displayTime > 0,
                        onStartClick = onStartClick,
                        onReset = onReset,
                        buttonWidth = 160.dp,
                        isVertical = false
                    )
                }
            }
        }
    }
}

@Composable
fun TimeDisplay(timeText: String, fontSize: TextUnit) {
    Text(
        text = timeText,
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace
        )
    )
}

@Composable
fun ControlButtons(
    isRunning: Boolean,
    isPaused: Boolean,
    showReset: Boolean,
    onStartClick: () -> Unit,
    onReset: () -> Unit,
    buttonWidth: Dp,
    isVertical: Boolean = false
) {
    val content = @Composable {
        Button(onClick = onStartClick, modifier = Modifier.width(buttonWidth)) {
            Text(text = stringResource(
                if (isRunning) R.string.measurement_stop_button
                else if (isPaused) R.string.measurement_resume_button
                else R.string.measurement_start_button
            ))
        }
        if (showReset) {
            if (isVertical) {
                Spacer(modifier = Modifier.width(Dimensions.SPACING_MEDIUM))
            } else {
                Spacer(modifier = Modifier.height(Dimensions.SPACING_SMALL))
            }
            OutlinedButton(onClick = onReset, modifier = Modifier.width(buttonWidth)) {
                Text(text = stringResource(R.string.measurement_reset_button))
            }
        }
    }

    if (isVertical) Row { content() } else Column { content() }
}

@Preview(showBackground = true)
@Composable
fun StopWatchPageScreenVerticalContentPreview() {
    SolveLogJetpackComposeTheme {
        StopwatchPageScreenContent(
            context = LocalContext.current,
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
            context = LocalContext.current,
            displayTime = 0L,
            isRunning = false,
            isPaused = false,
            isVertical = false,
            onToggleStartStop = {},
            onFormatTime = { "00:00:00.00" },
            onReset = {}
        )
    }
}
