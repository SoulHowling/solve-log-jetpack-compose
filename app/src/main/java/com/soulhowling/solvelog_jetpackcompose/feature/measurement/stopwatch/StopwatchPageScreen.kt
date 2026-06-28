package com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun StopwatchPageScreen(
    viewModel: StopwatchViewModel = viewModel()
) {
    val context = LocalContext.current
    val elapsedTime by viewModel.elapsedTime.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()
    StopWatchPageScreenContent(
        context = context,
        elapsedTime = elapsedTime,
        isRunning = isRunning,
        isPaused = isPaused,
        onToggleStartStop = viewModel::toggleStartStop,
        onFormatTime = viewModel::formatTime,
        onReset = viewModel::reset
    )
}

@Composable
fun StopWatchPageScreenContent(
    context: Context,
    elapsedTime: Long,
    isRunning: Boolean,
    isPaused: Boolean,
    onToggleStartStop: () -> Unit,
    onFormatTime: (Long) -> String,
    onReset: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onToggleStartStop()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = onFormatTime(elapsedTime),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp,
                    fontFamily = FontFamily.Monospace
                )
            )

            Spacer(modifier = Modifier.height(Dimensions.SPACING_LARGE))

            Row {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val status = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                            if (status != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onToggleStartStop()
                            }
                        } else {
                            onToggleStartStop()
                        }
                    },
                    modifier = Modifier.width(120.dp)
                ) {
                    Text(
                        text = if (isRunning) {
                            stringResource(R.string.measurement_stop_button)
                        } else if (isPaused) {
                            stringResource(R.string.measurement_resume_button)
                        } else {
                            stringResource(R.string.measurement_start_button)
                        }
                    )
                }

                if (elapsedTime > 0) {
                    Spacer(modifier = Modifier.width(Dimensions.SPACING_MEDIUM))
                    OutlinedButton(
                        onClick = {
                            onReset()
                        },
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text(text = stringResource(R.string.measurement_reset_button))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StopWatchPageScreenPreview() {
    SolveLogJetpackComposeTheme {
        StopWatchPageScreenContent(
            context = LocalContext.current,
            elapsedTime = 0L,
            isRunning = false,
            isPaused = false,
            onToggleStartStop = {},
            onFormatTime = { "00:00:00.00" },
            onReset = {}
        )
    }
}
