package com.soulhowling.solvelog_jetpackcompose.feature.memo.compose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.CustomAppBarEffect
import com.soulhowling.solvelog_jetpackcompose.core.LocalBottomBarVisibility
import com.soulhowling.solvelog_jetpackcompose.feature.memo.viewmodel.MemoEditorViewModel
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions

private val latexTemplates = listOf(
    "a/b" to "\\frac{a}{b}",
    "√x" to "\\sqrt{x}",
    "a^b" to "a^{b}",
    "a_i" to "a_{i}",
    "x̄" to "\\bar{x}",
    "x̂" to "\\hat{x}",
    "Σ" to "\\sum_{i=1}^{n}",
    "∫" to "\\int_{a}^{b}",
    "α" to "\\alpha",
    "β" to "\\beta",
    "γ" to "\\gamma",
    "δ" to "\\delta",
    "ε" to "\\epsilon",
    "ζ" to "\\zeta",
    "η" to "\\eta",
    "θ" to "\\theta",
    "ι" to "\\iota",
    "κ" to "\\kappa",
    "λ" to "\\lambda",
    "μ" to "\\mu",
    "ν" to "\\nu",
    "ξ" to "\\xi",
    "ο" to "o",
    "π" to "\\pi",
    "ρ" to "\\rho",
    "σ" to "\\sigma",
    "τ" to "\\tau",
    "υ" to "\\upsilon",
    "φ" to "\\phi",
    "χ" to "\\chi",
    "ψ" to "\\psi",
    "ω" to "\\omega",
    "ε" to "\\varepsilon",
    "θ" to "\\vartheta",
    "ρ" to "\\varrho",
    "σ" to "\\varsigma",
    "φ" to "\\varphi",
    "∞" to "\\infty",
    "~" to "\\sim",
    "≃" to "\\simeq",
    "≈" to "\\approx",
    "≒" to "\\fallingdotseq",
    "≓" to "\\risingdotseq",
    "≡" to "\\equiv",
    "×" to "\\times",
    "÷" to "\\div",
    "±" to "\\pm",
    "∓" to "\\mp",
    "≠" to "\\neq",
    "≥" to "\\geq",
    "≧" to "\\geqq",
    "≤" to "\\leq",
    "≦" to "\\leqq",
    "∈" to "\\in",
    "∋" to "\\ni",
    "∉" to "\\notin",
    "⊂" to "\\subset",
    "⊃" to "\\supset",
    "⊆" to "\\subseteq",
    "⊇" to "\\supseteq",
    "⊈" to "\\nsubseteq",
    "⊉" to "\\nsupseteq",
    "⊊" to "\\subsetneq",
    "⊋" to "\\supsetneq",
    "∪" to "\\cup",
    "∩" to "\\cap",
)

private val headingTemplates = listOf(
    "H1" to "# ",
    "H2" to "## ",
    "H3" to "### ",
    "H4" to "#### ",
    "H5" to "##### ",
    "H6" to "###### "
)

@Composable
fun MemoEditorScreen(
    memoId: Int,
    folderId: Int? = null,
    viewModel: MemoEditorViewModel = viewModel()
) {
    val name by viewModel.memoName.collectAsStateWithLifecycle()
    val content by viewModel.memoContent.collectAsStateWithLifecycle()

    LaunchedEffect(memoId, folderId) {
        viewModel.loadMemo(memoId, folderId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.forceSave()
        }
    }

    CustomAppBarEffect(
        title = stringResource(R.string.memo_editor_title),
    )

    MemoEditorScreenContent(
        name = name,
        content = content,
        onNameChange = { viewModel.onNameChange(it) },
        onContentChange = { viewModel.onContentChange(it) },
        onTemplateClick = { viewModel.insertTemplate(it) }
    )
}

@Composable
fun MemoEditorScreenContent(
    name: String,
    content: TextFieldValue,
    onNameChange: (String) -> Unit,
    onContentChange: (TextFieldValue) -> Unit,
    onTemplateClick: (String) -> Unit
) {
    val isBottomBarVisible = LocalBottomBarVisibility.current
    val bottomPadding = if (isBottomBarVisible) Dimensions.LIST_BOTTOM_WITH_FAB else 0.dp
    var showMathDialog by remember { mutableStateOf(false) }
    var showHeadingDialog by remember { mutableStateOf(false) }
    var showTableDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = bottomPadding + 16.dp)
    ) {
        TextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.memo_editor_title_label)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.headlineSmall,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp)
        ) {
            MarkdownButton(
                icon = Icons.Default.Title,
                contentDescription = "Heading",
                onClick = { showHeadingDialog = true }
            )
            MarkdownButton(
                icon = Icons.Default.FormatBold,
                contentDescription = "Bold",
                onClick = { onTemplateClick("**bold**") }
            )
            MarkdownButton(
                icon = Icons.Default.FormatItalic,
                contentDescription = "Italic",
                onClick = { onTemplateClick("*italic*") }
            )
            MarkdownButton(
                icon = painterResource(id = R.drawable.format_strikethrough),
                contentDescription = "strikethrough",
                onClick = { onTemplateClick("~~strikethrough~~") }
            )
            MarkdownButton(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                contentDescription = "List",
                onClick = { onTemplateClick("- ") }
            )
            MarkdownButton(
                icon = Icons.Default.FormatListNumbered,
                contentDescription = "Numbered List",
                onClick = { onTemplateClick("1. ") }
            )
            MarkdownButton(
                icon = Icons.Default.CheckBox,
                contentDescription = "Task",
                onClick = { onTemplateClick("- [ ] ") }
            )
            MarkdownButton(
                icon = Icons.Default.FormatQuote,
                contentDescription = "Quote",
                onClick = { onTemplateClick("> ") }
            )
            MarkdownButton(
                icon = Icons.Default.Code,
                contentDescription = "Code",
                onClick = { onTemplateClick("`code`") }
            )
            MarkdownButton(
                icon = rememberTextIconPainter(
                    text = "</>"
                ),
                contentDescription = "Code Block",
                onClick = { onTemplateClick("```\ncode block\n```") }
            )
            MarkdownButton(
                icon = painterResource(id = R.drawable.table),
                contentDescription = "Table",
                onClick = { showTableDialog = true }
            )
            MarkdownButton(
                icon = Icons.Default.HorizontalRule,
                contentDescription = "HorizontalRule",
                onClick = { onTemplateClick("\n---\n") }
            )
            MarkdownButton(
                icon = Icons.Default.Functions,
                contentDescription = "Math",
                onClick = { showMathDialog = true }
            )
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Write markdown here...") }
        )
    }

    if (showHeadingDialog) {
        TemplateDialog(
            title = stringResource(R.string.memo_editor_template_dialog_title_heading),
            onDismiss = { showHeadingDialog = false },
            onSelectTemplate = { snippet ->
                onTemplateClick(snippet)
                showHeadingDialog = false
            },
            templates = headingTemplates
        )
    }

    if (showMathDialog) {
        TemplateDialog(
            title = stringResource(R.string.memo_editor_template_dialog_title_latex),
            onDismiss = { showMathDialog = false },
            onSelectTemplate = { snippet ->
                onTemplateClick("$$$snippet$$")
                showMathDialog = false
            },
            templates = latexTemplates
        )
    }

    if (showTableDialog) {
        TableDialog(
            onDismiss = { showTableDialog = false },
            onConfirm = { snippet ->
                onTemplateClick(snippet)
                showTableDialog = false
            }
        )
    }
}

@Composable
fun TemplateDialog(
    title: String,
    onDismiss: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    templates: List<Pair<String, String>>,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
            ) {
                items(templates) { (label, snippet) ->
                    TextButton(
                        onClick = { onSelectTemplate(snippet) },
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_dialog_close_button))
            }
        }
    )
}

@Composable
private fun TableDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var rows by remember { mutableIntStateOf(2) }
    var cols by remember { mutableIntStateOf(3) }

    val generatedMarkdown = remember(rows, cols) {
        generateTableMarkdown(rows = rows, cols = cols)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.memo_editor_table_dialog_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NumberStepper(
                    label = stringResource(R.string.memo_editor_table_dialog_rows),
                    value = rows,
                    onValueChange = { rows = it },
                    range = 1..20
                )

                NumberStepper(
                    label = stringResource(R.string.memo_editor_table_dialog_cols),
                    value = cols,
                    onValueChange = { cols = it },
                    range = 1..20
                )

                HorizontalDivider()

                Column {
                    Text(
                        text = stringResource(R.string.memo_editor_table_dialog_preview),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                    ) {
                        Text(
                            text = generatedMarkdown.trim(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(generatedMarkdown) }
            ) {
                Text(text = stringResource(R.string.common_dialog_add_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_dialog_cancel_button))
            }
        }
    )
}

@Composable
private fun NumberStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { if (value > range.first) onValueChange(value - 1) },
                enabled = value > range.first
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease"
                )
            }

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
            )

            IconButton(
                onClick = { if (value < range.last) onValueChange(value + 1) },
                enabled = value < range.last
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase"
                )
            }
        }
    }
}

private fun generateTableMarkdown(rows: Int, cols: Int): String {
    val header = (1..cols).joinToString(" | ", prefix = "| ", postfix = " |") { "Header $it" }
    val separator = (1..cols).joinToString(" | ", prefix = "| ", postfix = " |") { "---" }
    val bodyRows = (1..rows).joinToString("\n") {
        (1..cols).joinToString(" | ", prefix = "| ", postfix = " |") { "Cell" }
    }
    return "\n$header\n$separator\n$bodyRows\n"
}

@Composable
fun MarkdownButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun MarkdownButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun rememberTextIconPainter(
    text: String,
    style: TextStyle = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 16.sp, // 24dpの枠に収まるサイズ
        fontFamily = FontFamily.SansSerif
    )
): Painter {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val contentColor = LocalContentColor.current

    // アイコンの標準サイズ（24dp）をピクセルに変換
    val sizePx = with(density) { 24.dp.toPx() }
    val sizeConstraints = Constraints.fixed(sizePx.toInt(), sizePx.toInt())

    // テキストの配置や色を計測
    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = style.copy(color = contentColor),
        constraints = sizeConstraints
    )

    return object : Painter() {
        // マテリアルアイコン標準の 24dp × 24dp を返す
        override val intrinsicSize = Size(sizePx, sizePx)

        override fun DrawScope.onDraw() {
            // 24dp の枠内の中央に配置するための位置（オフセット）を計算
            val x = (size.width - textLayoutResult.size.width + 8) / 2f
            val y = (size.height - textLayoutResult.size.height + 8) / 2f

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(x, y)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MemoEditorScreenContentPreview() {
    MemoEditorScreenContent(
        name = "Sample Memo",
        content = TextFieldValue("This is a sample memo content."),
        onNameChange = {},
        onContentChange = {},
        onTemplateClick = {}
    )
}
