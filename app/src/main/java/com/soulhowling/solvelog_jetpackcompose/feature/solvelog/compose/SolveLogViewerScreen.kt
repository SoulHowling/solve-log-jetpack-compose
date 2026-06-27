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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.CustomAppBarEffect
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
private val stickyColWidth = 56.dp
private val expandedStickyColWidth = 86.dp
private val spacing = 8.dp

@Composable
fun SolveLogViewerScreen(
    onNavigateToEditor: (Int) -> Unit,
    viewModel: SolveLogViewerViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val selectedIndices by viewModel.selectedQuestionIndices.collectAsStateWithLifecycle()

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var attemptToDelete by remember { mutableStateOf<Int?>(null) }

    val solveLog = (uiState as? SolveLogViewerUiState.Success)?.solveLog
    if (solveLog != null) {
        CustomAppBarEffect(
            title = solveLog.name,
            actions = {
                if (isEditMode) {
                    if (selectedIndices.isNotEmpty()) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.log_viewer_cd_delete_questions),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = stringResource(R.string.log_viewer_cd_cancel_edit),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.log_viewer_cd_edit),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.log_viewer_dialog_delete_questions_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.log_viewer_dialog_delete_questions_message,
                        selectedIndices.size
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelectedQuestions()
                        showDeleteConfirm = false
                    }
                ) {
                    Text(
                        stringResource(R.string.log_list_dialog_delete_button),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.common_dialog_cancel_button))
                }
            }
        )
    }

    if (attemptToDelete != null) {
        AlertDialog(
            onDismissRequest = { attemptToDelete = null },
            title = {
                Text(
                    stringResource(
                        R.string.log_viewer_dialog_delete_attempt_title,
                        attemptToDelete!!
                    )
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.log_viewer_dialog_delete_attempt_message,
                        attemptToDelete!!
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        attemptToDelete?.let { viewModel.deleteAttempt(it) }
                        attemptToDelete = null
                    }
                ) {
                    Text(
                        stringResource(R.string.log_list_dialog_delete_button),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { attemptToDelete = null }) {
                    Text(stringResource(R.string.common_dialog_cancel_button))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    isEditMode = isEditMode,
                    selectedIndices = selectedIndices,
                    onNavigateToEditor = onNavigateToEditor,
                    onStartNewAttempt = { viewModel.startNewAttempt(it) },
                    onAddQuestion = { viewModel.addQuestion() },
                    onToggleSelection = { viewModel.toggleQuestionSelection(it) },
                    onDeleteAttempt = { attemptToDelete = it }
                )
            }
            is SolveLogViewerUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.log_viewer_error_not_found),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SolveLogViewerScreenContent(
    solveLog: SolveLog,
    questions: Map<Int, List<Question>>,
    isEditMode: Boolean,
    selectedIndices: Set<Int>,
    onNavigateToEditor: (Int) -> Unit,
    onStartNewAttempt: ((Int) -> Unit) -> Unit,
    onAddQuestion: () -> Unit,
    onToggleSelection: (Int) -> Unit,
    onDeleteAttempt: (Int) -> Unit,
) {
    val horizontalScrollState = rememberScrollState()
    val questionsByAttempt = remember(questions) {
        questions.values.flatten().groupBy { it.attempt }
    }

    val dynamicStickyWidth = if (isEditMode) expandedStickyColWidth else stickyColWidth

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
                                    .size(dynamicStickyWidth, 32.dp)
                                    .graphicsLayer {
                                        translationX = horizontalScrollState.value.toFloat()
                                    }
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            )
                            repeat(solveLog.attemptCount) { index ->
                                val attemptNumber = index + 1
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (isEditMode) {
                                        IconButton(
                                            onClick = { onDeleteAttempt(attemptNumber) },
                                            modifier = Modifier
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.log_viewer_cd_delete_attempt),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    AttemptHeader(
                                        attemptNumber = attemptNumber,
                                        shape = headerShape,
                                        onNavigateToEditor = { onNavigateToEditor(attemptNumber) },
                                        width = colWidth
                                    )
                                }
                            }
                        }
                    }
                }

                // Scroll body
                items(solveLog.questionQuantity) { qIndex ->
                    val questionsForThisNumber = questions[qIndex] ?: emptyList()
                    val selectionType = SelectionType.entries.getOrElse(solveLog.selectionType) { SelectionType.ALPHABET }

                    Box(
                        modifier = Modifier.padding(start = 0.dp, end = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.width(dynamicStickyWidth))

                            repeat(solveLog.attemptCount) { aIndex ->
                                val attemptNumber = aIndex + 1
                                val question = questionsForThisNumber.find { it.attempt == attemptNumber }

                                SolveLogItemCell(
                                    selectedAnswer = question?.let { selectionType.getAnswerString(it.selectedAnswer) } ?: "-",
                                    correctness = question?.correctness ?: Correctness.NONE.index,
                                    isChecked = question?.isFlag ?: false,
                                    isLast = false, // Always show divider for cells now
                                    onNavigateToEditor = { if (!isEditMode) onNavigateToEditor(attemptNumber) },
                                    width = colWidth,
                                    height = rowHeight
                                )
                            }
                        }

                        // Question number label(Sticky left)
                        Box(
                            modifier = Modifier
                                .size(dynamicStickyWidth, rowHeight)
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
                            QuestionStickyLabel(
                                number = qIndex + 1,
                                isEditMode = isEditMode,
                                isSelected = selectedIndices.contains(qIndex),
                                onToggleSelection = { onToggleSelection(qIndex) }
                            )
                        }
                    }
                }

                // --- Add Question Row ---
                item {
                    Box(
                        modifier = Modifier.padding(start = 0.dp, end = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.width(dynamicStickyWidth))

                            Box(
                                modifier = Modifier
                                    .width(colWidth * solveLog.attemptCount + spacing * (solveLog.attemptCount - 1))
                                    .height(rowHeight)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { onAddQuestion() },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                            shape = MaterialTheme.shapes.small
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.log_viewer_button_add_question),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        // Sticky left placeholder for Add row
                        Box(
                            modifier = Modifier
                                .size(dynamicStickyWidth, rowHeight)
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
                                }
                        )
                    }
                }

                // --- 3. Sticky footer（Score） ---
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
                                    .size(dynamicStickyWidth, 48.dp)
                                    .graphicsLayer {
                                        translationX = horizontalScrollState.value.toFloat()
                                    }
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            )
                            repeat(solveLog.attemptCount) { aIndex ->
                                val attemptNumber = aIndex + 1
                                val questionsForAttempt = questionsByAttempt[attemptNumber] ?: emptyList()
                                val correctCount = questionsForAttempt.count { it.correctness == Correctness.CORRECT.index }

                                ScoreFooter(
                                    score = correctCount,
                                    total = solveLog.questionQuantity,
                                    shape = footerShape,
                                    width = colWidth,
                                    onNavigateToEditor = { onNavigateToEditor(attemptNumber) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (!isEditMode) {
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .graphicsLayer {
                        this.translationY = fabTranslationY.toPx()
                    }
                    .padding(all = Dimensions.PADDING_MEDIUM),
                onClick = {
                    onStartNewAttempt { nextAttempt ->
                        onNavigateToEditor(nextAttempt)
                    }
                }
            ) {
                Icon(Icons.Filled.Add, "Floating Action Button")
            }
        }
    }
}

@Composable
fun QuestionStickyLabel(
    number: Int,
    isEditMode: Boolean,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .then(if (isEditMode) Modifier.clickable { onToggleSelection() } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .size(32.dp, 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() },
                )
            }
            Spacer(Modifier.width(4.dp))
        }
        
        Box(
            modifier = Modifier
                .size(32.dp, 32.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
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
            text = stringResource(R.string.log_viewer_header_attempt, attemptNumber),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SolveLogItemCell(
    selectedAnswer: String,
    correctness: Int,
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
            correctness = correctness,
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
                text = stringResource(R.string.log_viewer_footer_score),
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
    correctness: Int,
    isChecked: Boolean
) {
    val correctnessType = Correctness.entries.getOrNull(correctness) ?: Correctness.NONE
    val isError = correctnessType == Correctness.INCORRECT
    
    Row(
        modifier = Modifier
            .fillMaxWidth(fraction = 0.9f)
            .height(32.dp)
            .clip(shape = MaterialTheme.shapes.small)
            .background(
                if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surfaceContainer
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedAnswer.ifEmpty { "-" },
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        val statusColor = when (correctnessType) {
            Correctness.CORRECT -> MaterialTheme.colorScheme.primary
            Correctness.INCORRECT -> MaterialTheme.colorScheme.error
            Correctness.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        
        Text(
            modifier = Modifier.weight(1f),
            text = correctnessType.symbol,
            color = statusColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = stringResource(R.string.log_viewer_cd_flag),
                modifier = Modifier.visible(isChecked),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
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
            correctness = Correctness.CORRECT.index,
            isFlag = true,
            solveLogId = solveLog.id
        ),
        Question(
            id = 2,
            attempt = 1,
            questionIndex = 1,
            selectedAnswer = 1,
            correctness = Correctness.CORRECT.index,
            isFlag = true,
            solveLogId = solveLog.id
        ),Question(
            id = 3,
            attempt = 2,
            questionIndex = 0,
            selectedAnswer = 2,
            correctness = Correctness.INCORRECT.index,
            isFlag = false,
            solveLogId = solveLog.id
        ),
    ).groupBy { it.questionIndex }
    SolveLogJetpackComposeTheme {
        SolveLogViewerScreenContent(
            solveLog = solveLog,
            questions = questions,
            isEditMode = true,
            selectedIndices = emptySet(),
            onNavigateToEditor = {},
            onStartNewAttempt = {},
            onAddQuestion = {},
            onToggleSelection = {},
            onDeleteAttempt = {},
        )
    }
}
