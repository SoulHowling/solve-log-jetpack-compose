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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
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
import com.soulhowling.solvelog_jetpackcompose.core.LocalAppBarController
import com.soulhowling.solvelog_jetpackcompose.core.LocalBottomBarVisibility
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.AnswerFormat
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.Correctness
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
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

object SolveLogViewerTestTags {
    const val VISIBILITY_TOGGLE = "visibility_toggle"
    const val DELETE_ATTEMPT = "delete_attempt_"
    const val ADD_QUESTION_ROW = "add_question_row"
    const val QUESTION_SELECTION = "question_selection_"
    const val ATTEMPT_HEADER = "attempt_header_"
    const val SCORE_FOOTER = "score_footer_"
    const val ITEM_CELL = "item_cell_"
    const val FAB_NEW_ATTEMPT = "fab_new_attempt"
    const val QUESTION_LABEL = "question_label_"
}

@Composable
fun SolveLogViewerScreen(
    onNavigateToEditor: (Int) -> Unit,
    viewModel: SolveLogViewerViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val selectedIndices by viewModel.selectedQuestionIndices.collectAsStateWithLifecycle()
    val isAnswerVisible by viewModel.isAnswerVisible.collectAsStateWithLifecycle()

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
                        stringResource(R.string.log_list_dropdown_menu_item_delete),
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

    val currentAttemptToDelete = attemptToDelete
    if (currentAttemptToDelete != null) {
        AlertDialog(
            onDismissRequest = { attemptToDelete = null },
            title = {
                Text(
                    stringResource(
                        R.string.log_viewer_dialog_delete_attempt_title,
                        currentAttemptToDelete
                    )
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.log_viewer_dialog_delete_attempt_message,
                        currentAttemptToDelete
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAttempt(currentAttemptToDelete)
                        attemptToDelete = null
                    }
                ) {
                    Text(
                        stringResource(R.string.log_list_dropdown_menu_item_delete),
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
                    isAnswerVisible = isAnswerVisible,
                    onNavigateToEditor = onNavigateToEditor,
                    onStartNewAttempt = { viewModel.startNewAttempt(it) },
                    onAddQuestion = { viewModel.addQuestion() },
                    onToggleSelection = { viewModel.toggleQuestionSelection(it) },
                    onDeleteAttempt = { attemptToDelete = it },
                    onAnswerVisibility = { viewModel.toggleAnswerVisibility() }
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
    isAnswerVisible: Boolean,
    onNavigateToEditor: (Int) -> Unit,
    onStartNewAttempt: ((Int) -> Unit) -> Unit,
    onAddQuestion: () -> Unit,
    onToggleSelection: (Int) -> Unit,
    onDeleteAttempt: (Int) -> Unit,
    onAnswerVisibility: () -> Unit,
) {
    val horizontalScrollState = rememberScrollState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val appBarController = LocalAppBarController.current

    LaunchedEffect(listState) {
        snapshotFlow { listState.canScrollForward || listState.canScrollBackward }
            .collect { canScroll ->
                if (!canScroll) {
                    appBarController.setBottomBarVisible(true)
                }
            }
    }

    val questionsByAttempt = remember(questions) {
        questions.values.flatten().groupBy { it.attempt }
    }

    val answerFormat = remember(solveLog.answerFormat) {
        AnswerFormat.from(solveLog.answerFormat)
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
                state = listState,
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
                            modifier = Modifier,
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(dynamicStickyWidth, 48.dp)
                                    .graphicsLayer {
                                        translationX = horizontalScrollState.value.toFloat()
                                    }
                                    .align(Alignment.Bottom)
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            ) {
                                if (answerFormat != AnswerFormat.FREE_RESPONSE) {
                                    IconButton(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .testTag(SolveLogViewerTestTags.VISIBILITY_TOGGLE),
                                        onClick = { onAnswerVisibility() },
                                    ) {
                                        Icon(
                                            imageVector = if (isAnswerVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = stringResource(R.string.log_viewer_cd_toggle_visibility),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            repeat(solveLog.attemptCount) { index ->
                                val attemptNumber = index + 1
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (isEditMode && solveLog.attemptCount > 1) {
                                        IconButton(
                                            onClick = { onDeleteAttempt(attemptNumber) },
                                            modifier = Modifier.testTag(SolveLogViewerTestTags.DELETE_ATTEMPT + attemptNumber)
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
                                        onNavigateToEditor = {
                                            if (!isEditMode) onNavigateToEditor(
                                                attemptNumber
                                            )
                                        },
                                        width = colWidth,
                                        modifier = Modifier.testTag(SolveLogViewerTestTags.ATTEMPT_HEADER + attemptNumber)
                                    )
                                }
                            }
                        }
                    }
                }

                // Scroll body
                items(solveLog.questionQuantity) { qIndex ->
                    val questionsForThisNumber = questions[qIndex] ?: emptyList()
                    val selectionType =
                        SelectionType.entries.getOrElse(solveLog.selectionType) { SelectionType.ALPHABET }

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
                                val question =
                                    questionsForThisNumber.find { it.attempt == attemptNumber }

                                SolveLogItemCell(
                                    selectedAnswer = question?.let {
                                        if (answerFormat == AnswerFormat.FREE_RESPONSE) {
                                            if (it.freeAnswerContent.isNullOrEmpty()) stringResource(SelectionType.unselectedResId) else it.freeAnswerContent
                                        } else {
                                            selectionType.getAnswerString(it.selectedAnswer)
                                        }
                                    } ?: stringResource(SelectionType.unselectedResId),
                                    answerFormat = answerFormat,
                                    correctness = question?.correctness ?: Correctness.NONE.index,
                                    isChecked = question?.isFlag ?: false,
                                    isLast = false, // Always show divider for cells now
                                    isAnswerVisible = isAnswerVisible,
                                    onNavigateToEditor = {
                                        if (!isEditMode) onNavigateToEditor(
                                            attemptNumber
                                        )
                                    },
                                    width = colWidth,
                                    height = rowHeight,
                                    modifier = Modifier.testTag(SolveLogViewerTestTags.ITEM_CELL + "${qIndex}_$attemptNumber")
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
                                    .testTag(SolveLogViewerTestTags.ADD_QUESTION_ROW)
                                    .clickable { onAddQuestion() },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                                val questionsForAttempt =
                                    questionsByAttempt[attemptNumber] ?: emptyList()
                                val correctCount =
                                    questionsForAttempt.count { it.correctness == Correctness.CORRECT.index }

                                ScoreFooter(
                                    score = correctCount,
                                    total = solveLog.questionQuantity,
                                    shape = footerShape,
                                    width = colWidth,
                                    onNavigateToEditor = { onNavigateToEditor(attemptNumber) },
                                    modifier = Modifier.testTag(SolveLogViewerTestTags.SCORE_FOOTER + attemptNumber)
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
                    .padding(all = Dimensions.PADDING_MEDIUM)
                    .testTag(SolveLogViewerTestTags.FAB_NEW_ATTEMPT),
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
                    modifier = Modifier.testTag(SolveLogViewerTestTags.QUESTION_SELECTION + number)
                )
            }
            Spacer(Modifier.width(4.dp))
        }

        Box(
            modifier = Modifier
                .size(32.dp, 32.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .testTag(SolveLogViewerTestTags.QUESTION_LABEL + number),
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
    modifier: Modifier = Modifier,
    onNavigateToEditor: () -> Unit
) {
    Box(
        modifier = modifier
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
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun SolveLogItemCell(
    selectedAnswer: String,
    answerFormat: AnswerFormat,
    correctness: Int,
    isChecked: Boolean,
    isLast: Boolean,
    isAnswerVisible: Boolean,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    onNavigateToEditor: () -> Unit,
) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val backgroundColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
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
            answerFormat = answerFormat,
            correctness = correctness,
            isChecked = isChecked,
            isAnswerVisible = isAnswerVisible
        )
    }
}

@Composable
fun ScoreFooter(
    score: Int,
    total: Int,
    shape: Shape,
    width: Dp,
    modifier: Modifier = Modifier,
    onNavigateToEditor: () -> Unit,
) {
    val accuracy: Float = if (total > 0) (score.toFloat() / total.toFloat()) * 100 else 0f
    Box(
        modifier = modifier
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
            Text(
                text = stringResource(R.string.log_viewer_accuracy_format, accuracy),
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
    answerFormat: AnswerFormat,
    correctness: Int,
    isChecked: Boolean,
    isAnswerVisible: Boolean,
) {
    val correctnessType = Correctness.entries.getOrNull(correctness) ?: Correctness.NONE
    val isError = correctnessType == Correctness.INCORRECT
    val unselectedString = stringResource(SelectionType.unselectedResId)
    val isUnselected = selectedAnswer == unselectedString || selectedAnswer.isEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth(fraction = 0.9f)
            .height(32.dp)
            .clip(shape = MaterialTheme.shapes.small)
            .background(
                if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isUnselected) {
                Text(
                    text = unselectedString,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else if (answerFormat == AnswerFormat.FREE_RESPONSE) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            } else if (!isAnswerVisible) {
                Text(
                    text = stringResource(R.string.log_viewer_invisible_answer),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = selectedAnswer,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

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
        answerFormat = AnswerFormat.MULTIPLE_CHOICE.index,
        selectionType = SelectionType.ALPHABET.index,
        selectionQuantity = SelectionQuantity.FOUR.index,
        attemptCount = 3,
        folderId = null
    )
    val questions = listOf(
        Question(
            id = 1,
            attempt = 1,
            questionIndex = 0,
            freeAnswerContent = null,
            selectedAnswer = 1,
            correctness = Correctness.CORRECT.index,
            isFlag = true,
            solveLogId = solveLog.id
        ),
        Question(
            id = 2,
            attempt = 1,
            questionIndex = 1,
            freeAnswerContent = null,
            selectedAnswer = 1,
            correctness = Correctness.CORRECT.index,
            isFlag = true,
            solveLogId = solveLog.id
        ),
        Question(
            id = 3,
            attempt = 2,
            questionIndex = 0,
            freeAnswerContent = null,
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
            isEditMode = false,
            selectedIndices = emptySet(),
            isAnswerVisible = true,
            onNavigateToEditor = {},
            onStartNewAttempt = {},
            onAddQuestion = {},
            onToggleSelection = {},
            onDeleteAttempt = {},
            onAnswerVisibility = {}
        )
    }
}
