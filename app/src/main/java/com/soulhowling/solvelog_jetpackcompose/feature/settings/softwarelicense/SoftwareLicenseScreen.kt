package com.soulhowling.solvelog_jetpackcompose.feature.settings.softwarelicense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.variant.LibraryActionKind
import com.mikepenz.aboutlibraries.util.withContext
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.CustomAppBarEffect
import com.soulhowling.solvelog_jetpackcompose.core.LocalBottomBarVisibility
import com.soulhowling.solvelog_jetpackcompose.ui.theme.Dimensions
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun SoftwareLicenseScreen() {
    val context = LocalContext.current
    val libraries = remember {
        Libs.Builder().withContext(context).build()
    }
    SoftwareLicenseScreenContent(libraries = libraries)
}

@Composable
fun SoftwareLicenseScreenContent(libraries: Libs?) {
    CustomAppBarEffect(
        title = stringResource(id = R.string.setting_software_license)
    )

    var selectedLibrary by remember { mutableStateOf<Library?>(null) }
    val isBottomBarVisible = LocalBottomBarVisibility.current
    val bottomPadding = if (isBottomBarVisible) Dimensions.LIST_BOTTOM_WITH_FAB else Dimensions.PADDING_MEDIUM

    val deduplicatedLibs = remember(libraries) {
        if (libraries == null) null
        else {
            val uniqueLibraries = libraries.libraries
                .groupBy { it.name.lowercase() }
                .map { (_, group) ->
                    group.firstOrNull { lib ->
                        lib.licenses.any { !it.licenseContent.isNullOrBlank() }
                    } ?: group.first()
                }
            libraries.copy(libraries = uniqueLibraries)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LibrariesContainer(
            libraries = deduplicatedLibs,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 0.dp,
                top = 0.dp,
                end = 0.dp,
                bottom = bottomPadding
            ),
            onActionClick = { library, actionKind ->
                if (actionKind == LibraryActionKind.License) {
                    selectedLibrary = library
                    true
                } else {
                    false
                }
            }
        )
    }

    val library = selectedLibrary
    if (library != null) {
        val licenseNames = library.licenses
            .mapNotNull { it.name.takeIf { name -> name.isNotBlank() } }
            .distinct()
            .joinToString(", ")

        val licenseContents = library.licenses
            .mapNotNull { lic ->
                lic.licenseContent?.takeIf { it.isNotBlank() }
                    ?: lic.url?.takeIf { it.isNotBlank() }
            }
            .distinct()

        val content = if (licenseContents.isNotEmpty()) {
            licenseContents.joinToString("\n\n----------------------------------------\n\n")
        } else {
            "No license content available."
        }

        AlertDialog(
            onDismissRequest = { selectedLibrary = null },
            title = {
                Column {
                    Text(
                        text = library.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (licenseNames.isNotBlank()) {
                        Text(
                            text = licenseNames,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            text = {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    Text(
                        text = content,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedLibrary = null }) {
                    Text(text = stringResource(R.string.common_dialog_close_button))
                }
            }
        )
    }
}

@Preview
@Composable
fun SoftwareLicenseScreenPreview() {
    SolveLogJetpackComposeTheme {
        SoftwareLicenseScreenContent(null)
    }
}
