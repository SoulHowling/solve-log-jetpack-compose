package com.soulhowling.solvelog_jetpackcompose.feature.memo.compose

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class MemoViewScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `MemoViewScreenContent renders empty content without crashing`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoViewScreenContent(
                    contentText = "",
                )
            }
        }
    }

    @Test
    fun `MemoViewScreenContent renders markdown text elements`() {
        val markdownContent = """
            # Heading Title
            This is plain text with **bold** and *italic*.
            - Bullet point 1
            - Bullet point 2
        """.trimIndent()

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoViewScreenContent(
                    contentText = markdownContent
                )
            }
        }

        composeTestRule.onNodeWithText("Heading Title", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Bullet point 1", substring = true).assertIsDisplayed()
    }

    @Test
    fun `MemoViewScreenContent renders code blocks and checklist items`() {
        val markdownContent = """
            - [ ] Uncompleted Task
            - [x] Completed Task
            
            ```kotlin
            val test = "hello"
            ```
        """.trimIndent()

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoViewScreenContent(
                    contentText = markdownContent
                )
            }
        }

        composeTestRule.onNodeWithText("Uncompleted Task", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Completed Task", substring = true).assertIsDisplayed()
    }
}
