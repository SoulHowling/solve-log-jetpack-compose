package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.core.LocalBottomBarVisibility
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.Correctness
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel.SolveLogViewerUiState
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel.SolveLogViewerViewModel
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import kotlin.Unit

private val rowHeight = 48.dp
private val colWidth = 120.dp
private val stickyColWidth = 44.dp
private val spacing = 8.dp

@Composable
fun SolveLogViewerScreen(
    onNavigateToEditor: () -> Unit,
    viewModel: SolveLogViewerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        is SolveLogViewerUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is SolveLogViewerUiState.Success -> {
            SolveLogViewerScreenContent(
                solveLog = state.solveLog,
                questions = state.questions,
                onNavigateToEditor = onNavigateToEditor
            )
        }
        is SolveLogViewerUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ログが見つかりませんでした",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SolveLogViewerScreenContent(
    solveLog: SolveLog,
    questions: Map<Int, List<Question>>,
    onNavigateToEditor: () -> Unit,
) {
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

    val isBottomBarVisible = LocalBottomBarVisibility.current
    val fabTranslationY by animateDpAsState(
        targetValue = if (isBottomBarVisible) (-80).dp else 0.dp,
        label = "fabTranslationY"
    )

    Box(modifier = Modifier.fillMaxSize()) {
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
                // Sticky header
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(start = 0.dp, end = 8.dp)
                            .drawBehind {
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
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            )
                            repeat(solveLog.attemptCount) { index ->
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

                // Scroll body
                items(solveLog.questionQuantity) { qIndex ->
                    val questionsForThisNumber = questions[qIndex] ?: emptyList()
                    val selectionType = SelectionType.entries[solveLog.selectionType]

                    Box(
                        modifier = Modifier.padding(start = 0.dp, end = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.width(stickyColWidth))

                            repeat(solveLog.attemptCount) { aIndex ->
                                val attemptNumber = aIndex + 1
                                val question = questionsForThisNumber.find { it.attempt == attemptNumber }

                                SolveLogItemCell(
                                    selectedAnswer = question?.let { selectionType.getAnswerString(it.selectedAnswer) } ?: "-",
                                    isCorrect = question?.isCorrect ?: true,
                                    isChecked = question?.isFlag ?: false,
                                    isLast = qIndex == solveLog.questionQuantity - 1,
                                    onNavigateToEditor = onNavigateToEditor,
                                    width = colWidth,
                                    height = rowHeight
                                )
                            }
                        }

                        // Question number label(Sticky left)
                        Box(
                            modifier = Modifier
                                .size(stickyColWidth, rowHeight)
                                .graphicsLayer {
                                    translationX = horizontalScrollState.value.toFloat()
                                }
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .drawBehind {
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

                // --- 3. Sticky footer（Score） ---
                item {
                    val questionsByAttempt = questions.values.flatten().groupBy { it.attempt }
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
                            repeat(solveLog.attemptCount) { aIndex ->
                                val attemptNumber = aIndex + 1
                                val questionsForAttempt = questionsByAttempt[attemptNumber] ?: emptyList()
                                val correctCount = questionsForAttempt.count { it.isCorrect }

                                ScoreFooter(
                                    score = correctCount,
                                    total = solveLog.questionQuantity,
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
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .graphicsLayer {
                    this.translationY = fabTranslationY.toPx()
                }
                .padding(all = Dimensions.PADDING_MEDIUM),
            onClick = onNavigateToEditor
        ) {
            Icon(Icons.Filled.Add, "Floating Action Button")
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
    shape: Shape,
    width: Dp,
    onNavigateToEditor: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .background(MaterialTheme.colorScheme.primaryContainer, shape)
            .clickable(onClick = { onNavigateToEditor() })
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
    score: Int,
    total: Int,
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
                text = "$score/$total",
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
            text = if (isCorrect) Correctness.CORRECT.symbol else Correctness.INCORRECT.symbol,
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
    val solveLog = SolveLog(
        id = 1,
        name = "模試第1回",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        questionQuantity = 10,
        selectionType = 0,
        selectionQuantity = 0,
        attemptCount = 3,
        folderId = null
    )
    val questions = listOf(
        Question(
            id = 1,
            attempt = 1,
            questionIndex = 0,
            selectedAnswer = 1,
            isCorrect = true,
            isFlag = true,
            solveLogId = solveLog.id
        ),
        Question(
            id = 2,
            attempt = 1,
            questionIndex = 1,
            selectedAnswer = 1,
            isCorrect = true,
            isFlag = true,
            solveLogId = solveLog.id
        ),Question(
            id = 3,
            attempt = 2,
            questionIndex = 0,
            selectedAnswer = 2,
            isCorrect = false,
            isFlag = false,
            solveLogId = solveLog.id
        ),
    ).groupBy { it.questionIndex }
    SolveLogJetpackComposeTheme {
        SolveLogViewerScreenContent(
            solveLog = solveLog,
            questions = questions,
            onNavigateToEditor = {}
        )
    }
}
