package com.soulhowling.solvelog_jetpackcompose.feature.settings.aboutthisapp

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soulhowling.solvelog_jetpackcompose.BuildConfig
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun AboutThisAppScreen() {
    AboutThisAppContent()
}

@Composable
fun AboutThisAppContent() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Image(
                painter = painterResource(id = R.mipmap.solve_log_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.about_this_app_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(48.dp))

        val notImplementedMessage = stringResource(id = R.string.common_not_implemented)
        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            onClick = {
                Toast.makeText(
                    context,
                    notImplementedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        ) {
            Text(text = stringResource(id = R.string.about_this_app_help_button))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutThisAppScreenPreview() {
    SolveLogJetpackComposeTheme {
        AboutThisAppContent()
    }
}
