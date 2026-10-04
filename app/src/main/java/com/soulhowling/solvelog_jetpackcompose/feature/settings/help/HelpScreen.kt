package com.soulhowling.solvelog_jetpackcompose.feature.settings.help

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.CustomAppBarEffect
import com.soulhowling.solvelog_jetpackcompose.core.LocalBottomBarVisibility
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun HelpScreen() {
    HelpScreenContent()
}

@Composable
fun HelpScreenContent() {
    CustomAppBarEffect(
        title = stringResource(id = R.string.about_this_app_help_button),
    )

    val scrollState = rememberScrollState()
    val isBottomBarVisible = LocalBottomBarVisibility.current
    val bottomPadding = if (isBottomBarVisible) Dimensions.LIST_BOTTOM_WITH_FAB else Dimensions.PADDING_MEDIUM

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(
                start = Dimensions.PADDING_MEDIUM,
                top = Dimensions.PADDING_MEDIUM,
                end = Dimensions.PADDING_MEDIUM,
                bottom = bottomPadding
            )
    ) {
        HelpSection(
            title = stringResource(id = R.string.help_section_logging_title),
            content = stringResource(id = R.string.help_section_logging_content)
        )

        Spacer(modifier = Modifier.height(24.dp))

        HelpSection(
            title = stringResource(id = R.string.help_section_measurement_title),
            content = stringResource(id = R.string.help_section_measurement_content)
        )

        Spacer(modifier = Modifier.height(24.dp))

        HelpSection(
            title = stringResource(id = R.string.help_section_memo_title),
            content = stringResource(id = R.string.help_section_memo_content)
        )

        Spacer(modifier = Modifier.height(24.dp))

        HelpSection(
            title = stringResource(id = R.string.help_section_settings_title),
            content = stringResource(id = R.string.help_section_settings_content)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HelpSection(
    title: String,
    content: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HelpScreenPreview() {
    SolveLogJetpackComposeTheme {
        HelpScreenContent()
    }
}
