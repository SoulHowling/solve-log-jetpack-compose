package com.soulhowling.solvelog_jetpackcompose.feature.settings.privacypolicy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.CustomAppBarEffect
import com.soulhowling.solvelog_jetpackcompose.core.LocalBottomBarVisibility
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun PrivacyPolicyScreen() {
    PrivacyPolicyScreenContent()
}

@Composable
fun PrivacyPolicyScreenContent() {
    CustomAppBarEffect(
        title = stringResource(id = R.string.setting_privacy_policy)
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
        Text(
            text = stringResource(id = R.string.privacy_policy_content),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrivacyPolicyScreenPreview() {
    SolveLogJetpackComposeTheme {
        PrivacyPolicyScreenContent()
    }
}
