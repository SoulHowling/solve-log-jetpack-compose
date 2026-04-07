package com.soulhowling.solvelog_jetpackcompose.feature.editor.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun SolveLogEditorScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimensions.PADDING_MEDIUM,
                top = Dimensions.PADDING_MEDIUM,
                end = Dimensions.PADDING_MEDIUM,
                bottom = Dimensions.LIST_BOTTOM_WITH_FAB
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING_MEDIUM)
        ) {
            items(10) { index ->
                SolveLogItemCard(
                    title = "Log #${index + 1}",
                    description = "Description for Log #${index + 1}"
                )
            }
        }

        NewLogButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Dimensions.PADDING_MEDIUM),
            onClick = { /* TODO */ }
        )
    }
}

@Composable
fun SolveLogItemCard(
    title: String, description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.SPACING_SMALL),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(Dimensions.PADDING_MEDIUM)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = Dimensions.PADDING_SMALL)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}

@Composable
fun NewLogButton(modifier: Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },
        modifier = modifier
    ) {
        Icon(Icons.Filled.Add, "Floating Action Button")
    }
}

@Preview(showBackground = true)
@Composable
fun SolveLogEditorScreenPreview() {
    SolveLogJetpackComposeTheme {
        SolveLogEditorScreen()
    }
}
