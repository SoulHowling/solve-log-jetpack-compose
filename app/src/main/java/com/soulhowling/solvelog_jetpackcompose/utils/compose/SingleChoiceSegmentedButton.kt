package com.soulhowling.solvelog_jetpackcompose.utils.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

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
private fun SingleChoiceSegmentedButtonPreview() {
    SolveLogJetpackComposeTheme {
        SingleChoiceSegmentedButton(
            modifier = Modifier.fillMaxWidth(),
            itemList = listOf("項目1", "項目2", "項目3"),
            selectedIndex = 0,
            onSelectedIndexChange = {},
            isEnabled = true
        )
    }
}