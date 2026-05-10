package com.soulhowling.solvelog_jetpackcompose.feature.solvelog

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.LocalBottomBarVisibility
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun SolveLogListScreen(onNavigateToViewer: () -> Unit) {
    SolveLogListScreenContent(onNavigateToViewer = onNavigateToViewer)
}

@Composable
fun SolveLogListScreenContent(onNavigateToViewer: () -> Unit) {
    var isShownBottomSheet by remember { mutableStateOf(false) }

    val isBottomBarVisible = LocalBottomBarVisibility.current

    val fabTranslationY by animateDpAsState(
        targetValue = if (isBottomBarVisible) (-80).dp else 0.dp,
        label = "fabTranslationY"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimensions.PADDING_MEDIUM,
                top = Dimensions.PADDING_MEDIUM,
                end = Dimensions.PADDING_MEDIUM,
                bottom = Dimensions.LIST_BOTTOM_WITH_FAB
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING_MEDIUM)
        ) {
            items(10) { index ->
                SolveLogItemCard(
                    title = "Log #${index + 1}",
                    description = "Description for Log #${index + 1}",
                    onClick = onNavigateToViewer
                )
            }
        }

        NewLogButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .graphicsLayer {
                    this.translationY = fabTranslationY.toPx()
                }
                .padding(Dimensions.PADDING_MEDIUM),
            onClick = { isShownBottomSheet = true }
        )

        if (isShownBottomSheet) {
            SolveLogAddBottomSheet(
                onDismiss = { isShownBottomSheet = false }
            )
        }
    }
}

@Composable
fun SolveLogItemCard(
    title: String, description: String, onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.SPACING_SMALL),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(Dimensions.PADDING_MEDIUM)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = Dimensions.PADDING_SMALL)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}

@Composable
fun NewLogButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },
        modifier = modifier
    ) {
        Icon(Icons.Filled.Add, stringResource(id = R.string.talkback_new_log_button))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolveLogAddBottomSheet(
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
    ) {
        SolveLogAddContent(
            onDismiss = onDismiss
        )
    }
}


@Composable
fun SolveLogAddContent(
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()
    val itemKindList = SelectionType.entries.map { it.displayString }
    val itemQuantityList = SelectionQuantity.entries.map { it.displayString }
    
    var kindSelectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var quantitySelectedIndex by rememberSaveable { mutableIntStateOf(0) }
    
    val isTrueFalseSelected = SelectionType.entries[kindSelectedIndex] == SelectionType.CORRECTNESS

    val sectionSpace = Dimensions.SPACING_MEDIUM

    var logTitle by rememberSaveable { mutableStateOf("") }
    var questionQuantity by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.PADDING_MEDIUM)
    ) {
        item {
            Text(
                text = "新しいログを追加",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimensions.PADDING_MEDIUM),
                textAlign = TextAlign.Center
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "ログタイトル",
                    style = MaterialTheme.typography.titleMedium,
                )
                TextField(
                    value = logTitle,
                    onValueChange = {
                        logTitle = it
                    },
                    placeholder = { Text("例: 統計調査士 2019年過去問") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(sectionSpace))

                Text(
                    text = "問題数",
                    style = MaterialTheme.typography.titleMedium
                )
                TextField(
                    value = questionQuantity,
                    onValueChange = {
                        questionQuantity = it
                    },
                    placeholder = { Text("例: 100") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(sectionSpace))


                Text(
                    text = "選択肢の種類",
                    style = MaterialTheme.typography.titleMedium
                )
                SingleChoiceSegmentedButton(
                    modifier = Modifier.fillMaxWidth(),
                    itemList = itemKindList,
                    selectedIndex = kindSelectedIndex,
                    onSelectedIndexChange = { kindSelectedIndex = it },
                    isEnabled = true
                )
                Spacer(modifier = Modifier.height(sectionSpace))


                Text(
                    text = "選択肢の数",
                    style = MaterialTheme.typography.titleMedium
                )
                SingleChoiceSegmentedButton(
                    modifier = Modifier.fillMaxWidth(),
                    itemList = itemQuantityList,
                    selectedIndex = if (isTrueFalseSelected) SelectionQuantity.TWO.ordinal else quantitySelectedIndex,
                    onSelectedIndexChange = { quantitySelectedIndex = it },
                    isEnabled = !isTrueFalseSelected
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                enabled = isAddButtonEnabled(logTitle = logTitle, questionQuantity = questionQuantity)
            ) {
                Text("追加")
            }
        }

        item {
            Spacer(modifier = Modifier.height(Dimensions.SPACING_LARGE))
        }
    }
}

private fun isAddButtonEnabled(logTitle: String, questionQuantity: String): Boolean {
    return logTitle.isNotBlank() && questionQuantity.isNotBlank()
}

@Composable
fun SingleChoiceSegmentedButton(
    modifier: Modifier = Modifier,
    itemList: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    isEnabled: Boolean
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier,
    ) {
        itemList.forEachIndexed { index, label ->
            SegmentedButton(
                enabled = isEnabled,
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = itemList.size
                ),
                onClick = { onSelectedIndexChange(index) },
                selected = index == selectedIndex,
                label = { Text(label) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SolveLogListScreenContentPreview() {
    SolveLogJetpackComposeTheme {
        SolveLogListScreenContent(onNavigateToViewer = {})
    }
}

@Preview(showBackground = true)
@Composable
fun SolveLogAddContentPreview() {
    SolveLogJetpackComposeTheme {
        SolveLogAddContent(
            onDismiss = {}
        )
    }
}
