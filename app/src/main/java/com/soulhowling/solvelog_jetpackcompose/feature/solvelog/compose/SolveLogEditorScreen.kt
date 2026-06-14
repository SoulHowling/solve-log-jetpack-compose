package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.Correctness
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel.SolveLogEditorUiState
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel.SolveLogEditorViewModel
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun SolveLogEditorScreen(
    viewModel: SolveLogEditorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is SolveLogEditorUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is SolveLogEditorUiState.Success -> {
            SolveLogEditorScreenContent(
                solveLog = state.solveLog,
                questions = state.questions,
                selectionTypeStringList = state.selectionTypeStringList,
                onUpdate = { selectedAnswer, questionIndex, correctness, isFlag ->
                    viewModel.updateQuestion(selectedAnswer, questionIndex, correctness, isFlag)
                }
            )
        }
        is SolveLogEditorUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("データの読み込みに失敗しました")
            }
        }
    }
}

@Composable
fun SolveLogEditorScreenContent(
    solveLog: SolveLog,
    questions: List<Question>,
    selectionTypeStringList: List<String>,
    onUpdate: (Int, Int, Correctness, Boolean) -> Unit
) {
    var isHeaderVisible by remember { mutableStateOf(true) }
    val questionsMap = remember(questions) { questions.associateBy { it.questionIndex } }

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
            items(
                count = solveLog.questionQuantity,
                key = { index ->
                    questionsMap[index]?.id ?: "empty_$index"
                },
            ) { index ->
                val question = questionsMap[index]
                val selectionType = SelectionType.entries.getOrElse(solveLog.selectionType) { SelectionType.ALPHABET }

                SolveLogEditorRowItem(
                    number = index + 1,
                    initialAnswer = question?.let { selectionType.getAnswerString(it.selectedAnswer) } ?: "",
                    initialCorrectness = question?.let { Correctness.entries.getOrNull(it.correctness) } ?: Correctness.NONE,
                    initialIsFlagged = question?.isFlag ?: false,
                    onUpdate = { selectedAnswer, correctness, isFlag ->
                        onUpdate(selectedAnswer, index, correctness, isFlag)
                    },
                    modifier = Modifier.animateItem(),
                    selectionTypeStringList = selectionTypeStringList
                )
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
                Text(
                    text = "解答",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1.0f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

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
fun SolveLogEditorRowItem(
    number: Int,
    initialAnswer: String,
    initialCorrectness: Correctness?,
    initialIsFlagged: Boolean,
    onUpdate: (Int, Correctness, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    selectionTypeStringList: List<String>
) {
    var isExpandedDropdown by rememberSaveable { mutableStateOf(false) }
    var selectedAnswer by remember(initialAnswer) { mutableStateOf(initialAnswer) }
    var resultStatus by remember(initialCorrectness) { mutableStateOf(initialCorrectness) }
    var isCheckedFlag by remember(initialIsFlagged) { mutableStateOf(initialIsFlagged) }

    ListItem(
        modifier = modifier
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
                // 2. Selected answer
                Box(
                    modifier = Modifier.weight(1.0f),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedCard(
                        onClick = { isExpandedDropdown = true },
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
                                text = selectedAnswer,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    DropdownMenu(expanded = isExpandedDropdown, onDismissRequest = { isExpandedDropdown = false }) {

                        selectionTypeStringList.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedAnswer = option
                                    isExpandedDropdown = false
                                    val answerIndex = selectionTypeStringList.indexOf(option)
                                    onUpdate(answerIndex, resultStatus ?: Correctness.NONE, isCheckedFlag)
                                }
                            )
                        }
                    }
                }

                // 3. Correctness
                Box(
                    modifier = Modifier.weight(1.2f),
                    contentAlignment = Alignment.Center
                ) {
                    CorrectIncorrectToggleButton(
                        selectedItem = resultStatus,
                        onItemSelected = {
                            resultStatus = it
                            val answerIndex = selectionTypeStringList.indexOf(selectedAnswer)
                            onUpdate(answerIndex, it, isCheckedFlag)
                        }
                    )
                }
            }
        },
        trailingContent = {
            // 4. Flag
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = isCheckedFlag,
                    onCheckedChange = {
                        isCheckedFlag = it
                        val answerIndex = selectionTypeStringList.indexOf(selectedAnswer)
                        onUpdate(answerIndex, resultStatus ?: Correctness.NONE, it)
                    },
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    )
}

@Composable
fun CorrectIncorrectToggleButton(
    selectedItem: Correctness?,
    onItemSelected: (Correctness) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember { Correctness.entries.filter { it != Correctness.NONE } }
    val selectedIndex = items.indexOf(selectedItem)

    val indicatorColor = when (selectedItem) {
        Correctness.CORRECT -> MaterialTheme.colorScheme.primary
        Correctness.INCORRECT -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.surface
    }

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

            if (selectedIndex != -1) {
                val offset by animateDpAsState(
                    targetValue = itemWidth * selectedIndex,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "SelectionOffset"
                )
                Surface(
                    modifier = Modifier
                        .offset {
                            IntOffset(
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
                                MaterialTheme.colorScheme.surface
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
    val solveLog = SolveLog(
        id = 1,
        name = "テストログ",
        questionQuantity = 10,
        selectionType = SelectionType.ALPHABET.index,
        selectionQuantity = 4,
        attemptCount = 1,
        createdAt = 0L,
        updatedAt = 0L,
        folderId = null
    )
    SolveLogJetpackComposeTheme {
        SolveLogEditorScreenContent(
            solveLog = solveLog,
            questions = emptyList(),
            selectionTypeStringList = listOf("1", "2", "3", "4"),
            onUpdate = { _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SolveLogHeaderPreview() {
    SolveLogJetpackComposeTheme {
        SolveLogHeader()
    }
}

@Preview(
    showBackground = true,
    heightDp = 80
)
@Composable
fun SolveLogEditorRowItemPreview() {
    SolveLogJetpackComposeTheme {
        SolveLogEditorRowItem(
            number = 1,
            initialAnswer = "A",
            initialCorrectness = Correctness.CORRECT,
            initialIsFlagged = false,
            onUpdate = { _, _, _ -> },
            selectionTypeStringList = listOf("A", "B", "C", "D")
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CorrectIncorrectToggleButtonPreview() {
    SolveLogJetpackComposeTheme {
        var selectedItem by remember { mutableStateOf<Correctness?>(null) }
        CorrectIncorrectToggleButton(
            selectedItem = selectedItem,
            onItemSelected = { selectedItem = it }
        )
    }
}
