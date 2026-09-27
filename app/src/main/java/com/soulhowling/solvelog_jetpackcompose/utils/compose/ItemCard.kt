package com.soulhowling.solvelog_jetpackcompose.utils.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.SPACING_SMALL),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(Dimensions.PADDING_MEDIUM)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = Dimensions.PADDING_MEDIUM)
                        .size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                content()
            }

            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@Composable
fun ItemOverflowMenu(
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onMove: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onExport: (() -> Unit)? = null,
    customMenuItems: (@Composable () -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(
            onClick = { isExpanded = true }
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.log_list_cd_more_options)
            )
        }
        DropdownMenu(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .clip(RoundedCornerShape(16.dp)),
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            if (onEdit != null) {
                DropdownMenuItem(
                    onClick = {
                        isExpanded = false
                        onEdit()
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.log_list_dropdown_menu_item_edit),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
            if (onMove != null) {
                DropdownMenuItem(
                    onClick = {
                        isExpanded = false
                        onMove()
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.log_list_dropdown_menu_item_move),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
            if (onDelete != null) {
                DropdownMenuItem(
                    onClick = {
                        isExpanded = false
                        onDelete()
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.log_list_dropdown_menu_item_delete),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
            if (onExport != null) {
                DropdownMenuItem(
                    onClick = {
                        isExpanded = false
                        onExport()
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.log_list_dropdown_menu_item_export),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
            customMenuItems?.invoke()
        }
    }
}

@Composable
fun ItemDragIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription = stringResource(R.string.log_list_cd_drag_handle),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemCardPreview() {
    SolveLogJetpackComposeTheme {
        ItemCard(
            modifier = Modifier,
            icon = Icons.Default.Edit,
            onClick = {},
            enabled = true,
            trailingContent = {
                ItemOverflowMenu(
                    onEdit = {},
                    onMove = {},
                    onDelete = {},
                    onExport = {}
                )
            }
        ){
            Text(
                text = "This is a card",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
