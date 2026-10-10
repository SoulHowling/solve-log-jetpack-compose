package com.soulhowling.solvelog_jetpackcompose.feature.memo.compose

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class MemoEditorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `MemoEditorScreenContent displays initial title and content`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoEditorScreenContent(
                    name = "My Test Title",
                    content = TextFieldValue("My Test Content"),
                    onNameChange = {},
                    onContentChange = {},
                    onTemplateClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("My Test Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("My Test Content").assertIsDisplayed()
    }

    @Test
    fun `typing in title field triggers onNameChange`() {
        var updatedName: String? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoEditorScreenContent(
                    name = "",
                    content = TextFieldValue(""),
                    onNameChange = { updatedName = it },
                    onContentChange = {},
                    onTemplateClick = {},
                )
            }
        }

        val titleLabel = context.getString(R.string.memo_editor_title_label)
        composeTestRule.onNodeWithText(titleLabel, substring = true).performTextInput("New Memo Title")
        assertEquals("New Memo Title", updatedName)
    }

    @Test
    fun `markdown toolbar buttons insert corresponding formatting`() {
        var insertedSnippet: String? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoEditorScreenContent(
                    name = "Title",
                    content = TextFieldValue(""),
                    onNameChange = {},
                    onContentChange = {},
                    onTemplateClick = { insertedSnippet = it },
                )
            }
        }

        // Bold
        composeTestRule.onNodeWithContentDescription("Bold").performScrollTo().performClick()
        assertEquals("**bold**", insertedSnippet)

        // Italic
        composeTestRule.onNodeWithContentDescription("Italic").performScrollTo().performClick()
        assertEquals("*italic*", insertedSnippet)

        // Strikethrough
        composeTestRule.onNodeWithContentDescription("strikethrough").performScrollTo().performClick()
        assertEquals("~~strikethrough~~", insertedSnippet)

        // Bullet List
        composeTestRule.onNodeWithContentDescription("List").performScrollTo().performClick()
        assertEquals("- ", insertedSnippet)

        // Numbered List
        composeTestRule.onNodeWithContentDescription("Numbered List").performScrollTo().performClick()
        assertEquals("1. ", insertedSnippet)

        // Task
        composeTestRule.onNodeWithContentDescription("Task").performScrollTo().performClick()
        assertEquals("- [ ] ", insertedSnippet)

        // Quote
        composeTestRule.onNodeWithContentDescription("Quote").performScrollTo().performClick()
        assertEquals("> ", insertedSnippet)

        // Code
        composeTestRule.onNodeWithContentDescription("Code").performScrollTo().performClick()
        assertEquals("`code`", insertedSnippet)

        // Code Block
        composeTestRule.onNodeWithContentDescription("Code Block").performScrollTo().performClick()
        assertEquals("```\ncode block\n```", insertedSnippet)

        // HorizontalRule
        composeTestRule.onNodeWithContentDescription("HorizontalRule").performScrollTo().performClick()
        assertEquals("\n---\n", insertedSnippet)
    }

    @Test
    fun `heading button opens TemplateDialog and selects heading template`() {
        var insertedSnippet: String? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoEditorScreenContent(
                    name = "Title",
                    content = TextFieldValue(""),
                    onNameChange = {},
                    onContentChange = {},
                    onTemplateClick = { insertedSnippet = it },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Heading").performScrollTo().performClick()
        composeTestRule.onNode(hasText("H1"), useUnmergedTree = true).performClick()

        assertEquals("# ", insertedSnippet)
    }

    @Test
    fun `template dialog close button dismisses dialog`() {
        var insertedSnippet: String? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoEditorScreenContent(
                    name = "Title",
                    content = TextFieldValue(""),
                    onNameChange = {},
                    onContentChange = {},
                    onTemplateClick = { insertedSnippet = it },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Heading").performScrollTo().performClick()

        val closeButtonText = context.getString(R.string.common_dialog_close_button)
        composeTestRule.onNode(hasText(closeButtonText), useUnmergedTree = true).performClick()

        assertNull(insertedSnippet)
    }

    @Test
    fun `math button opens TemplateDialog and selects LaTeX template`() {
        var insertedSnippet: String? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoEditorScreenContent(
                    name = "Title",
                    content = TextFieldValue(""),
                    onNameChange = {},
                    onContentChange = {},
                    onTemplateClick = { insertedSnippet = it },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Math").performScrollTo().performClick()
        composeTestRule.onNode(hasText("a/b"), useUnmergedTree = true).performClick()

        assertEquals("$\\frac{a}{b}$", insertedSnippet)
    }

    @Test
    fun `table button opens TableDialog and confirms table markdown insertion`() {
        var insertedSnippet: String? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoEditorScreenContent(
                    name = "Title",
                    content = TextFieldValue(""),
                    onNameChange = {},
                    onContentChange = {},
                    onTemplateClick = { insertedSnippet = it },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Table").performScrollTo().performClick()

        val addButtonText = context.getString(R.string.common_dialog_add_button)
        composeTestRule.onNode(hasText(addButtonText), useUnmergedTree = true).performClick()

        assertNotNull(insertedSnippet)
        assertTrue(insertedSnippet!!.contains("| Header 1 | Header 2 | Header 3 |"))
    }

    @Test
    fun `table dialog steppers increase rows and columns`() {
        var insertedSnippet: String? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoEditorScreenContent(
                    name = "Title",
                    content = TextFieldValue(""),
                    onNameChange = {},
                    onContentChange = {},
                    onTemplateClick = { insertedSnippet = it },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Table").performScrollTo().performClick()

        // Click Increase on first stepper (rows from 2 to 3)
        composeTestRule.onAllNodesWithContentDescription("Increase", useUnmergedTree = true)[0].performClick()

        val addButtonText = context.getString(R.string.common_dialog_add_button)
        composeTestRule.onNode(hasText(addButtonText), useUnmergedTree = true).performClick()

        assertNotNull(insertedSnippet)
        val cellCount = insertedSnippet!!.split("Cell").size - 1
        // 3 rows x 3 cols = 9 cells
        assertEquals(9, cellCount)
    }

    @Test
    fun `table dialog cancel button dismisses dialog`() {
        var insertedSnippet: String? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoEditorScreenContent(
                    name = "Title",
                    content = TextFieldValue(""),
                    onNameChange = {},
                    onContentChange = {},
                    onTemplateClick = { insertedSnippet = it },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Table").performScrollTo().performClick()

        val cancelButtonText = context.getString(R.string.common_dialog_cancel_button)
        composeTestRule.onNode(hasText(cancelButtonText), useUnmergedTree = true).performClick()

        assertNull(insertedSnippet)
    }
}
