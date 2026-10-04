package com.soulhowling.solvelog_jetpackcompose.feature.memo.compose

import android.content.Context
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.CustomAppBarEffect
import com.soulhowling.solvelog_jetpackcompose.feature.memo.viewmodel.MemoViewViewModel
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.linkify.LinkifyPlugin

@Composable
fun MemoViewScreen(
    memoId: Int,
    onNavigateToEditor: (Int) -> Unit,
    viewModel: MemoViewViewModel = viewModel()
) {
    val memo by viewModel.memo.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val density = LocalDensity.current
    val paddingPx = with(density) { 16.dp.toPx() }.toInt()

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
        context = context,
        density = density,
        paddingPx = paddingPx,
        textColor = textColor,
        contentText = contentText,
    )
}

@Composable
private fun MemoViewScreenContent(
    context: Context,
    density: Density,
    paddingPx: Int,
    textColor: Int,
    contentText: String,
    modifier: Modifier = Modifier
) {
    val markwon = remember(context, density) {
        createMarkdownConfig(context, density)
    }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AndroidView(
            factory = { ctx ->
                TextView(ctx).apply {
                    setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                    setTextColor(textColor)
                    textSize = 16f
                    movementMethod = LinkMovementMethod.getInstance()
                }
            },
            update = { textView ->
                LogUtils.d("text: $contentText")
                markwon.setMarkdown(textView, contentText)
            },
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        )
    }
}

private fun createMarkdownConfig(context: Context, density: Density): Markwon {
    val textSizePx = with(density) { 16.sp.toPx() }
    return Markwon.builder(context)
        .usePlugin(SoftBreakAddsNewLinePlugin.create())
        .usePlugin(TablePlugin.create(context))
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(TaskListPlugin.create(context))
        .usePlugin(MarkwonInlineParserPlugin.create())
        .usePlugin(JLatexMathPlugin.create(textSizePx) { builder ->
            builder.inlinesEnabled(true)
        })
        .build()
}

@Preview(showBackground = true)
@Composable
private fun MemoViewScreenContentPreview() {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val density = LocalDensity.current
    val paddingPx = with(density) { 16.dp.toPx() }.toInt()
    SolveLogJetpackComposeTheme {
        MemoViewScreenContent(
            context = context,
            density = density,
            paddingPx = paddingPx,
            textColor = textColor,
            contentText = "test\n~~test~~\n**Bold**",
        )
    }
}
