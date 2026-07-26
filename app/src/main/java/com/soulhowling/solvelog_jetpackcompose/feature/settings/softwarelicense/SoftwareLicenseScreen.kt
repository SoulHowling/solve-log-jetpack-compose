package com.soulhowling.solvelog_jetpackcompose.feature.settings.softwarelicense

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.util.withContext
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.CustomAppBarEffect
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun SoftwareLicenseScreen() {
    SoftwareLicenseScreenContent()
}

@Composable
fun SoftwareLicenseScreenContent() {
    CustomAppBarEffect(
        title = stringResource(id = R.string.setting_software_license)
    )

    val context = LocalContext.current
    val libraries = remember {
        Libs.Builder().withContext(context).build()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LibrariesContainer(
            libraries = libraries,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
fun SoftwareLicenseScreenPreview() {
    SolveLogJetpackComposeTheme {
        SoftwareLicenseScreenContent()
    }
}
