package com.soulhowling.solvelog_jetpackcompose.feature.measurement.compose

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
import androidx.compose.ui.unit.dp
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    LogUtils.d("ストップウォッチ開始")
                }
            ) {
                Text(text = "開始")
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