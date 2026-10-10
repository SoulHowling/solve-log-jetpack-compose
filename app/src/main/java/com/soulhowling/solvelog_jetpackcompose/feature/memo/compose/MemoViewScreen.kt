package com.soulhowling.solvelog_jetpackcompose.feature.memo.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrm.markdown.parser.MarkdownParser
import com.hrm.markdown.parser.ast.ContainerNode
import com.hrm.markdown.parser.ast.FencedCodeBlock
import com.hrm.markdown.parser.ast.Node
import com.hrm.markdown.renderer.Markdown
import com.hrm.markdown.renderer.MarkdownTheme
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.CustomAppBarEffect
import com.soulhowling.solvelog_jetpackcompose.feature.memo.viewmodel.MemoViewViewModel
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun MemoViewScreen(
    memoId: Int,
    onNavigateToEditor: (Int) -> Unit,
    viewModel: MemoViewViewModel = viewModel()
) {
    val memo by viewModel.memo.collectAsStateWithLifecycle()
    LaunchedEffect(memoId) {
        viewModel.loadMemo(memoId)
    }

    val name = memo?.name.orEmpty()
    val contentText = memo?.content.orEmpty()
    val appBarTitle = name.ifBlank { stringResource(R.string.memo_title) }

    CustomAppBarEffect(
        title = appBarTitle,
        actions = {
            IconButton(onClick = { onNavigateToEditor(memoId) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )

    MemoViewScreenContent(
        contentText = contentText,
    )
}

@Composable
fun MemoViewScreenContent(
    contentText: String,
    modifier: Modifier = Modifier
) {
    val processedMarkdown = remember(contentText) {
        contentText
            .replace(Regex("(?<!\n)\n(?!\n)"), "  \n")
            .replace(Regex("(?m)^(\\s*(?:[-*+]|\\d+\\.)\\s*\\[[ xX]] )(\\S)")) { match ->
                "${match.groupValues[1]}\u200C\u00A0\u00A0\u00A0${match.groupValues[2]}"
            }
    }

    val parser = remember { MarkdownParser() }
    val document = remember(processedMarkdown) {
        val doc = parser.parse(processedMarkdown)
        fun fixCodeBlocks(node: Node) {
            if (node is FencedCodeBlock) {
                node.literal = node.literal.trimEnd('\n', '\r')
            }
            if (node is ContainerNode) {
                node.children.forEach { fixCodeBlocks(it) }
            }
        }
        fixCodeBlocks(doc)
        doc
    }

    val colorScheme = MaterialTheme.colorScheme
    val baseTheme = MarkdownTheme.material3()
    val customTheme = remember(baseTheme, colorScheme) {
        baseTheme.copy(
            bodyStyle = baseTheme.bodyStyle.copy(
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.None
                )
            ),
            inlineCodeStyle = SpanStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                background = colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            inlineCodeBackground = colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    }
    Surface(
        modifier = modifier.fillMaxSize()
            .padding(all = Dimensions.PADDING_SMALL),
        color = MaterialTheme.colorScheme.background
    ) {
        Markdown(
            document = document,
            modifier = Modifier.fillMaxSize(),
            theme = customTheme
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MemoViewScreenContentPreview() {
    val contentText = """
        test
        ~~test~~
        **Test**
        - 箇条書き1
        - 箇条書き2
        1. 番号付き
        2. 番号付き
    """.trimIndent()
    SolveLogJetpackComposeTheme {
        MemoViewScreenContent(
            contentText = contentText,
        )
    }
}
