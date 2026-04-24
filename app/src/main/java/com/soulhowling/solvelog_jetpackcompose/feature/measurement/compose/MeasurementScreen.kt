package com.soulhowling.solvelog_jetpackcompose.feature.measurement.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch.StopWatchPageScreen
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.TimerPageScreen
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import kotlinx.coroutines.launch

@Composable
fun MeasurementScreen() {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            val titles = listOf("Stopwatch", "Timer") // TODO
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> StopWatchPageScreen()
                1 -> TimerPageScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MeasurementScreenPreview() {
    SolveLogJetpackComposeTheme {
        MeasurementScreen()
    }
}
