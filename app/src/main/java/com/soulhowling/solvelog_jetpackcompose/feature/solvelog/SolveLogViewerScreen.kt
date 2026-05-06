package com.soulhowling.solvelog_jetpackcompose.feature.solvelog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import kotlin.Unit

private val rowHeight = 48.dp
private val colWidth = 120.dp
private val stickyColWidth = 44.dp
private val spacing = 8.dp

private const val attemptCount = 10 // TODO: 仮置き
private const val questionCount = 20 // TODO: 仮置き

@Composable
fun SolveLogViewerScreen(onNavigateToEditor: () -> Unit) {
    SolveLogViewerScreenContent(onNavigateToEditor = onNavigateToEditor)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SolveLogViewerScreenContent(onNavigateToEditor: () -> Unit) {
    val horizontalScrollState = rememberScrollState()

    val headerShape = MaterialTheme.shapes.medium.copy(
        bottomStart = CornerSize(0.dp),
        bottomEnd = CornerSize(0.dp)
    )
    val footerShape = MaterialTheme.shapes.medium.copy(
        topStart = CornerSize(0.dp),
        topEnd = CornerSize(0.dp)
    )
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Box(modifier = Modifier.fillMaxSize()) {
        // 全体を横スクロール可能にするコンテナ
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .horizontalScroll(horizontalScrollState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // --- 1. 固定ヘッダー（第n回） ---
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(start = 0.dp, end = 8.dp)
                            .drawBehind {
                                // 固定列の右端に細い境界線を描画
                                drawLine(
                                    color = dividerColor,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            },
                    ) {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(stickyColWidth, 32.dp)
                                    .graphicsLayer {
                                        translationX = horizontalScrollState.value.toFloat()
                                    }
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow) // 不透明背景で重なりを防止
                            )
                            repeat(attemptCount) { index ->
                                AttemptHeader(
                                    attemptNumber = index + 1,
                                    shape = headerShape,
                                    onNavigateToEditor = onNavigateToEditor,
                                    width = colWidth
                                )
                            }
                        }
                    }
                }

                // --- 2. スクロールボディ（問題番号 + 解答データ） ---
                items(questionCount) { qIndex ->
                    Box(
                        modifier = Modifier.padding(start = 0.dp, end = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // スペーサーで解答データの位置を確保
                            Box(modifier = Modifier.width(stickyColWidth))

                            repeat(attemptCount) {
                                // TODO: ここは実際のデータに基づいて表示内容を変える必要がある
                                SolveLogItemCell(
                                    selectedAnswer = if (qIndex % 3 == 0) "B" else "A",
                                    isCorrect = qIndex % 4 != 0,
                                    isChecked = qIndex % 5 == 0,
                                    isLast = qIndex == questionCount - 1,
                                    onNavigateToEditor = onNavigateToEditor,
                                    width = colWidth,
                                    height = rowHeight
                                )
                            }
                        }

                        // 問題番号ラベル：graphicsLayer で左端に固定
                        Box(
                            modifier = Modifier
                                .size(stickyColWidth, rowHeight)
                                .graphicsLayer {
                                    translationX = horizontalScrollState.value.toFloat()
                                }
                                .background(MaterialTheme.colorScheme.surfaceContainerLow) // 不透明背景で下の文字を隠す
                                .drawBehind {
                                    // 固定列の右端に細い境界線を描画
                                    drawLine(
                                        color = dividerColor,
                                        start = Offset(size.width, 0f),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            QuestionStickyLabel(number = qIndex + 1, height = 32.dp)
                        }
                    }
                }

                // --- 3. 固定フッター（Score） ---
                item {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(end = 8.dp, bottom = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(stickyColWidth, 48.dp)
                                    .graphicsLayer {
                                        translationX = horizontalScrollState.value.toFloat()
                                    }
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            )
                            repeat(attemptCount) {
                                ScoreFooter(
                                    shape = footerShape,
                                    width = colWidth,
                                    onNavigateToEditor = onNavigateToEditor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionStickyLabel(
    number: Int,
    height: Dp
) {
    Box(
        modifier = Modifier
            .size(32.dp, height)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$number",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AttemptHeader(
    attemptNumber: Int,
    shape: Shape, width: Dp,
    onNavigateToEditor: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .background(MaterialTheme.colorScheme.primaryContainer, shape)
            .clickable(onClick = { onNavigateToEditor() } )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "第 $attemptNumber 回",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SolveLogItemCell(
    selectedAnswer: String,
    isCorrect: Boolean,
    isChecked: Boolean,
    isLast: Boolean,
    onNavigateToEditor: () -> Unit,
    width: Dp,
    height: Dp
) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val backgroundColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .size(width, height)
            .drawBehind {
                drawRect(backgroundColor)
                if (!isLast) {
                    drawLine(
                        color = dividerColor,
                        start = Offset(8.dp.toPx(), size.height),
                        end = Offset(size.width - 8.dp.toPx(), size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            .clickable(onClick = { onNavigateToEditor() }),
        contentAlignment = Alignment.Center
    ) {
        SolveLogRowItem(
            selectedAnswer = selectedAnswer,
            isCorrect = isCorrect,
            isChecked = isChecked
        )
    }
}

@Composable
fun ScoreFooter(
    shape: Shape,
    width: Dp,
    onNavigateToEditor: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(width)
            .background(color = MaterialTheme.colorScheme.surfaceVariant, shape)
            .padding(all = 5.dp)
            .clickable(onClick = { onNavigateToEditor() }),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Score",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "100/100",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SolveLogRowItem(
    selectedAnswer: String,
    isCorrect: Boolean,
    isChecked: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(fraction = 0.9f)
            .height(32.dp)
            .clip(shape = MaterialTheme.shapes.small)
            .background(
                if (isCorrect) MaterialTheme.colorScheme.surfaceContainer
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedAnswer,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (isCorrect) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        val statusColor =
            if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        Text(
            modifier = Modifier.weight(1f),
            text = if (isCorrect) CorrectIncorrectItem.CORRECT.symbol else CorrectIncorrectItem.INCORRECT.symbol,
            color = statusColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Icon(
            imageVector = Icons.Default.Flag,
            contentDescription = "フラグ",
            modifier = Modifier
                .weight(1f)
                .visible(isChecked),
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SolveLogViewerScreenPreview() {
    SolveLogJetpackComposeTheme {
        SolveLogViewerScreenContent(onNavigateToEditor = {})
    }
}
