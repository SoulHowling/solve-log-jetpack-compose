package com.soulhowling.solvelog_jetpackcompose.feature.measurement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions

@Composable
fun ControlButtons(
    isRunning: Boolean,
    isPaused: Boolean,
    isCompleted: Boolean,
    showReset: Boolean,
    onStartClick: () -> Unit,
    onReset: () -> Unit,
    isVertical: Boolean = false
) {
    val buttonWidth = if (isVertical) 120.dp else 160.dp
    val content = @Composable {
        Button(
            onClick = onStartClick,
            modifier = Modifier
                .width(buttonWidth),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = stringResource(
                    id = if (isCompleted) R.string.measurement_restart_button
                    else if (isRunning) R.string.measurement_stop_button
                    else if (isPaused) R.string.measurement_resume_button
                    else R.string.measurement_start_button
                )
            )
        }
        if (showReset) {
            if (isVertical) {
                Spacer(modifier = Modifier.width(Dimensions.SPACING_MEDIUM))
            } else {
                Spacer(modifier = Modifier.height(Dimensions.SPACING_SMALL))
            }
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.width(buttonWidth),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(text = stringResource(R.string.measurement_reset_button))
            }
        }
    }

    if (isVertical) Row { content() } else Column { content() }
}

@Preview(showBackground = true)
@Composable
fun ControlButtonsPreview() {
    Column {
        ControlButtons(
            isRunning = true,
            isPaused = false,
            isCompleted = false,
            showReset = true,
            onStartClick = {},
            onReset = {},
            isVertical = false
        )
        ControlButtons(
            isRunning = true,
            isPaused = false,
            isCompleted = false,
            showReset = true,
            onStartClick = {},
            onReset = {},
            isVertical = true
        )
    }
}