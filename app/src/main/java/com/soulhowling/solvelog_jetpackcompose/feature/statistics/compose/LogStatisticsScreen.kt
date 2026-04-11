package com.soulhowling.solvelog_jetpackcompose.feature.statistics.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun LogStatisticsScreen() {
    // TODO
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.PADDING_MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING_MEDIUM_LARGE)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SPACING_MEDIUM_LARGE)
        ) {
            StatCard(
                title = "Accuracy",
                value = "15.43s",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total study time",
                value = "13.22s",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SPACING_MEDIUM_LARGE)
        ) {
            StatCard(
                title = "Count",
                value = "15.43s",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total time",
                value = "13.22s",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.PADDING_MEDIUM),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogStatisticsScreenPreview() {
    SolveLogJetpackComposeTheme {
        LogStatisticsScreen()
    }
}