package com.soulhowling.solvelog_jetpackcompose.feature.settings

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import com.soulhowling.solvelog_jetpackcompose.ui.theme.ThemeMode
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils
import com.soulhowling.solvelog_jetpackcompose.utils.compose.SingleChoiceSegmentedButton

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    SettingScreenContent(
        themeMode = themeMode,
        onThemeModeChange = { viewModel.setThemeMode(it) }
    )
}

@Composable
fun SettingScreenContent(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val settingsOptions = remember {
        listOf(
            SettingItem.Notification,
            SettingItem.AppTheme,
            SettingItem.WipeData,
            SettingItem.AboutThisApp
        )
    }
    val themeModeList: List<String> = ThemeMode.entries.map { stringResource(it.labelResId) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(settingsOptions) { option ->
            when (option) {
                is SettingItem.AppTheme -> {
                    SettingItem(
                        titleResId = option.titleResId,
                        onClick = { /* nop */ },
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
                        }
                    )
                }

                else -> {
                    SettingItem(
                        titleResId = option.titleResId,
                        onClick = {
                            LogUtils.d("$option がクリックされた")
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun SettingItem(
    @StringRes titleResId: Int,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(text = stringResource(id = titleResId)) },
        supportingContent = supportingContent,
        trailingContent = trailingContent,
        modifier = Modifier.clickable { onClick() }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    SolveLogJetpackComposeTheme {
        SettingScreenContent(
            themeMode = ThemeMode.SYSTEM,
            onThemeModeChange = {}
        )
    }
}