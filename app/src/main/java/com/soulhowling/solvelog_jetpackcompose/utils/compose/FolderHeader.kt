package com.soulhowling.solvelog_jetpackcompose.utils.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions

@Composable
fun CurrentFolderHeader(
    currentFolderPath: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    backButtonTestTag: String? = null,
) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.PADDING_MEDIUM, vertical = Dimensions.PADDING_SMALL)
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onNavigateBack() },
            modifier = if (backButtonTestTag != null) modifier.testTag(backButtonTestTag) else modifier
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.log_list_cd_back)
            )
        }
        Text(
            text = currentFolderPath,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier
                .weight(1f)
                .padding(start = Dimensions.PADDING_SMALL)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CurrentFolderHeaderPreview() {
    CurrentFolderHeader(
        currentFolderPath = "Current Folder",
        onNavigateBack = {},
    )
}