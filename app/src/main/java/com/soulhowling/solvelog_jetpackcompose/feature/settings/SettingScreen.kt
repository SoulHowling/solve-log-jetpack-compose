package com.soulhowling.solvelog_jetpackcompose.feature.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import com.soulhowling.solvelog_jetpackcompose.ui.theme.ThemeMode
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils
import com.soulhowling.solvelog_jetpackcompose.utils.compose.SingleChoiceSegmentedButton

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = viewModel(),
    onNavigateToWipeData: () -> Unit = {},
    onNavigateToAboutThisApp: () -> Unit = {}
) {
    val themeMode by viewModel.themeMode.collectAsState()
    SettingScreenContent(
        themeMode = themeMode,
        onThemeModeChange = { viewModel.setThemeMode(it) },
        onWipeData = onNavigateToWipeData,
        onAboutThisApp = onNavigateToAboutThisApp
    )
}

@Composable
fun SettingScreenContent(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWipeData: () -> Unit,
    onAboutThisApp: () -> Unit,
) {
    val context = LocalContext.current
    val notImplementedMessage = stringResource(id = R.string.common_not_implemented)
    val settingsItems = remember(onWipeData, onAboutThisApp, context) {
        val notYetImplementedAction = { Toast.makeText(context, notImplementedMessage, Toast.LENGTH_SHORT).show() }
        listOf(
            SettingItem.AppTheme(),
            SettingItem.WipeData(onWipeData),
            SettingItem.AboutThisApp(onAboutThisApp),
            SettingItem.PrivacyPolicy(notYetImplementedAction),
            SettingItem.SoftwareLicense(notYetImplementedAction)
        )
    }
    val themeModeList: List<String> = ThemeMode.entries.map { stringResource(it.labelResId) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = settingsItems,
            key = { it.titleResId }
        ) { item ->
            when (item) {
                is SettingItem.AppTheme -> {
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = item.titleResId)) },
                        supportingContent = {
                            SingleChoiceSegmentedButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                itemList = themeModeList,
                                selectedIndex = ThemeMode.entries.indexOf(themeMode),
                                onSelectedIndexChange = { index ->
                                    val mode = ThemeMode.entries[index]
                                    onThemeModeChange(mode)
                                    LogUtils.d("テーマ設定変更: $mode")
                                },
                                isEnabled = true
                            )
                        },
                        modifier = Modifier
                    )
                }

                else -> {
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = item.titleResId)) },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null
                            )
                        },
                        modifier = item.action?.let { action ->
                            Modifier.clickable { action() }
                        } ?: Modifier
                    )
                }
            }
            HorizontalDivider()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    SolveLogJetpackComposeTheme {
        SettingScreenContent(
            themeMode = ThemeMode.SYSTEM,
            onThemeModeChange = {},
            onWipeData = {},
            onAboutThisApp = {}
        )
    }
}