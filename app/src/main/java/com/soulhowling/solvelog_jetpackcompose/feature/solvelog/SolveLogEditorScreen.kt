package com.soulhowling.solvelog_jetpackcompose.feature.solvelog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun SolveLogEditorScreen() {
    SolveLogEditorScreenContent()
}

@Composable
fun SolveLogEditorScreenContent() {
    // 状態管理（デモ用）
    var logList by remember { mutableStateOf((1..20).toList()) } // TODO

    // ヘッダーの表示状態
    var isHeaderVisible by remember { mutableStateOf(true) }

    // スクロール検知用の NestedScrollConnection
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (consumed.y < -5) {
                    isHeaderVisible = false
                } else if (consumed.y > 5) {
                    isHeaderVisible = true
                }
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // ヘッダーを AnimatedVisibility で囲む
        AnimatedVisibility(
            visible = isHeaderVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + expandVertically() + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + shrinkVertically() + fadeOut()
        ) {
            SolveLogHeader()
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(
                items = logList,
                key = { _, id -> id }
            ) { index, id ->
                val onDeleteItem = remember(id) {
                    { logList = logList.filter { it != id } }
                }

                SolveLogItem(
                    number = index + 1,
                    onDelete = onDeleteItem,
                    modifier = Modifier.animateItem()
                )
            }

            item {
                TextButton(
                    onClick = {
                        val nextId = (logList.maxOrNull() ?: 0) + 1
                        logList = logList + nextId
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("新しいアイテムを追加")
                }
            }
        }
    }
}

@Composable
fun SolveLogHeader() {
    ListItem(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .height(40.dp)
            .clip(MaterialTheme.shapes.medium),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        leadingContent = {
            Text(
                text = "No.",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(32.dp),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        },
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 解答
                Text(
                    text = "解答",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1.0f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // 正誤
                Text(
                    text = "正誤",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1.2f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        trailingContent = {
            // 復習
            Text(
                text = "フラグ",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(48.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    )
}

@Composable
fun SolveLogItem(
    number: Int,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: 内部状態（本来はViewModel等で管理するのが望ましい）
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("A") }
    var resultStatus by remember { mutableStateOf<CorrectIncorrectItem?>(null) }
    var isChecked by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(color),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "削除",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(end = 48.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "削除",
                    modifier = Modifier.padding(end = 16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        modifier = modifier
    ) {
        ListItem(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(MaterialTheme.shapes.medium),
            tonalElevation = 1.dp,
            leadingContent = {
                Text(
                    text = "$number.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(32.dp),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            },
            headlineContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 2. 解答
                    Box(
                        modifier = Modifier.weight(1.0f),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedCard(
                            onClick = { expanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = selectedOption,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("A", "B", "C").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedOption = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // 3. 正誤
                    Box(
                        modifier = Modifier.weight(1.2f),
                        contentAlignment = Alignment.Center
                    ) {
                        CorrectIncorrectToggleButton(
                            selectedItem = resultStatus,
                            onItemSelected = { resultStatus = it }
                        )
                    }
                }
            },
            trailingContent = {
                // 4. 復習
                Box(
                    modifier = Modifier.weight(0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it },
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        )
    }
}

@Composable
fun CorrectIncorrectToggleButton(
    selectedItem: CorrectIncorrectItem?,
    onItemSelected: (CorrectIncorrectItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = CorrectIncorrectItem.entries
    val selectedIndex = items.indexOf(selectedItem)

    // 選択状態に応じた色設定
    val indicatorColor = when (selectedItem) {
        CorrectIncorrectItem.CORRECT -> MaterialTheme.colorScheme.primary
        CorrectIncorrectItem.INCORRECT -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.surface
    }

    // 外側のコンテナ（カプセル形状）
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.height(46.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
        ) {
            val itemWidth = maxWidth / items.size

            // 選択中のインジケーター（スライドする背景）
            if (selectedIndex != -1) {
                val offset by animateDpAsState(
                    targetValue = itemWidth * selectedIndex,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "SelectionOffset"
                )
                Surface(
                    modifier = Modifier
                        .offset {
                            androidx.compose.ui.unit.IntOffset(
                                x = offset.roundToPx(),
                                y = 0
                            )
                        }
                        .width(itemWidth)
                        .fillMaxHeight(),
                    shape = CircleShape,
                    color = indicatorColor,
                    shadowElevation = 2.dp
                ) {}
            }

            // 各アイテムのテキストとクリック領域
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEach { item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onItemSelected(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.symbol,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = if (selectedItem == item) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedItem == item) {
                                Color.White
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SolveLogEditorScreenContentPreview() {
    SolveLogJetpackComposeTheme {
        SolveLogEditorScreenContent()
    }
}

@Preview(showBackground = true)
@Composable
fun SolveLogHeaderPreview() {
    SolveLogJetpackComposeTheme {
        SolveLogHeader()
    }
}

@Preview(showBackground = true)
@Composable
fun SolveLogItemPreview() {
    SolveLogJetpackComposeTheme {
        SolveLogItem(
            number = 1,
            onDelete = { /* TODO */ }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CorrectIncorrectToggleButtonPreview() {
    SolveLogJetpackComposeTheme {
        var selectedItem by remember { mutableStateOf<CorrectIncorrectItem?>(null) }
        CorrectIncorrectToggleButton(
            selectedItem = selectedItem,
            onItemSelected = { selectedItem = it }
        )
    }
}