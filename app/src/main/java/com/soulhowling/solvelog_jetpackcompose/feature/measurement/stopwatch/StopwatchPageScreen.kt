package com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils

@Composable
fun StopWatchPageScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "00:00:00",
                style = MaterialTheme.typography.displayLarge
            )
            
            Spacer(modifier = Modifier.height(Dimensions.SPACING_LARGE))
            
            Button(
                onClick = {
                    LogUtils.d("ストップウォッチ開始") // TODO
                }
            ) {
                Text(text = "開始") // TODO
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StopWatchPageScreenPreview() {
    SolveLogJetpackComposeTheme {
        StopWatchPageScreen()
    }
}