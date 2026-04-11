package com.soulhowling.solvelog_jetpackcompose.feature.measurement.compose

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils
import java.util.Locale

@Composable
fun TimerPageScreen() {
    val timers = List(10) { (it + 1) * 60 } // TODO

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
            items(timers) { time ->
                TimerItemCard(time = time)
            }
        }

        NewTimerButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Dimensions.PADDING_MEDIUM),
            onClick = { /** TODO **/ }
        )
    }
}

@Composable
fun TimerItemCard(time: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.SPACING_SMALL),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(Dimensions.PADDING_MEDIUM)) {
            Text(
                text = String.format(Locale.ROOT, "%02d:00", time / 60),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { 0.5f },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { LogUtils.d("タイマー開始: $time 秒") }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "開始") // TODO
                }
                IconButton(onClick = { LogUtils.d("タイマー一時停止: $time 秒") }) {
                    Icon(Icons.Default.Pause, contentDescription = "一時停止") // TODO
                }
                IconButton(onClick = { LogUtils.d("タイマーリセット: $time 秒") }) {
                    Icon(Icons.Default.Refresh, contentDescription = "リセット") // TODO
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
        Icon(Icons.Filled.Add, "Floating Action Button") // TODO
    }
}

@Preview(showBackground = true)
@Composable
fun TimerPageScreenPreview() {
    SolveLogJetpackComposeTheme {
        TimerPageScreen()
    }
}
