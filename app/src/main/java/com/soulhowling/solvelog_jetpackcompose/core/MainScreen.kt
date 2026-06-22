package com.soulhowling.solvelog_jetpackcompose.core

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

@Composable
fun MainScreen(
    bottomNaviItems: List<Screens>,
    allScreens: List<Screens>
) {
    val activity = LocalActivity.current as ComponentActivity
    val viewModel: MainViewModel = viewModel(activity)
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    LaunchedEffect(currentDestination) {
        viewModel.setActions(null)

        if (bottomNaviItems.any { it.route == currentDestination?.route }) {
            viewModel.setTitle(null)
        }
    }

    val customTitle by viewModel.customTitle.collectAsStateWithLifecycle()
    val appBarActions by viewModel.appBarActions.collectAsStateWithLifecycle()

    val canNavigateBack = navController.previousBackStackEntry != null &&
            bottomNaviItems.none { it.route == currentDestination?.route }

    var isBottomBarVisible by rememberSaveable { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (consumed.y < -5) {
                    isBottomBarVisible = false
                } else if (consumed.y > 5) {
                    isBottomBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            TopAppBar(
                currentDestination = currentDestination,
                canNavigateBack = canNavigateBack,
                onBackClick = { navController.navigateUp() },
                allScreens = allScreens,
                customTitle = customTitle,
                actions = appBarActions
            )
        }
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalBottomBarVisibility provides isBottomBarVisible,
            LocalAppBarController provides viewModel
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavGraph(
                    navController = navController,
                    innerPadding = innerPadding
                )

                Box(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    AnimatedVisibility(
                        visible = isBottomBarVisible,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it }),
                    ) {
                        BottomNavigation(navController, currentDestination, bottomNaviItems)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    currentDestination: NavDestination?,
    canNavigateBack: Boolean,
    onBackClick: () -> Unit,
    allScreens: List<Screens>,
    customTitle: String? = null,
    actions: (@Composable () -> Unit)? = null
) {
    val currentScreen = allScreens.find { it.route == currentDestination?.route }
    
    CenterAlignedTopAppBar(
        title = {
            if (customTitle != null) {
                Text(text = customTitle, textAlign = TextAlign.Center)
            } else {
                val title = currentScreen?.title ?: R.string.error
                Text(
                    text = stringResource(id = title),
                    textAlign = TextAlign.Center
                )
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        actions = {
            if (actions != null) {
                actions()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun BottomNavigation(
    navController: NavHostController,
    currentDestination: NavDestination?,
    items: List<Screens>
) {

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(stringResource(screen.title)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun CustomAppBarEffect(
    title: String?,
    actions: (@Composable () -> Unit)? = null
) {
    val controller = LocalAppBarController.current
    val currentAction by rememberUpdatedState(actions)
    LaunchedEffect(title, actions == null) {
        controller.setTitle(title)
        if (actions == null) {
            controller.setActions(null)
        }
        controller.setActions { currentAction?.invoke() }
    }
}

@Preview(showBackground = true)
@Composable
fun TopAppBarPreview() {
    SolveLogJetpackComposeTheme {
        Column {
            TopAppBar(
                currentDestination = null,
                canNavigateBack = false,
                onBackClick = {},
                allScreens = Screens.getAllScreens()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TopAppBar(
                currentDestination = null,
                canNavigateBack = true,
                onBackClick = {},
                allScreens = Screens.getAllScreens()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationPreview() {
    SolveLogJetpackComposeTheme {
        BottomNavigation(
            navController = rememberNavController(),
            currentDestination = null,
            items = Screens.getBottomNaviScreens()
        )
    }
}