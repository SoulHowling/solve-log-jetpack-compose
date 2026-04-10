package com.soulhowling.solvelog_jetpackcompose.feature.settings.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils

@Composable
fun SettingScreen() {
    val settingsOptions = listOf(
        SettingItem.Notification,
        SettingItem.DarkTheme,
        SettingItem.AboutThisApp
    )
    var isDarkTheme by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(settingsOptions) { option ->
            SettingItem(
                titleResId = option.titleResId,
                onClick = {
                    LogUtils.d("$option がクリックされた")
                },
                trailingContent = {
                    when (option) {
                        is SettingItem.DarkTheme -> {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = {
                                    isDarkTheme = it
                                    LogUtils.d("ダークテーマ設定変更: $it")
                                }
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                    }
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun SettingItem(
    @StringRes titleResId: Int,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(text = stringResource(id = titleResId)) },
        trailingContent = trailingContent,
        modifier = Modifier.clickable { onClick() }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    SolveLogJetpackComposeTheme {
        SettingScreen()
    }
}