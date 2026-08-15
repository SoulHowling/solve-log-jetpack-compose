package com.soulhowling.solvelog_jetpackcompose.feature.measurement

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
import androidx.compose.ui.res.stringResource
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch.StopwatchPageScreen
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.compose.TimerListPageScreen
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import kotlinx.coroutines.launch

@Composable
fun MeasurementScreen(
    onNavigateToTimerPageScreen: (Int) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            val titles = listOf(
                R.string.measurement_stopwatch_title,
                R.string.measurement_timer_title
            )
            titles.forEachIndexed { index, titleResId ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = stringResource(titleResId)) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> StopwatchPageScreen()
                1 -> TimerListPageScreen(onNavigateToTimerPageScreen = onNavigateToTimerPageScreen)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MeasurementScreenPreview() {
    SolveLogJetpackComposeTheme {
        MeasurementScreen(onNavigateToTimerPageScreen = {})
    }
}