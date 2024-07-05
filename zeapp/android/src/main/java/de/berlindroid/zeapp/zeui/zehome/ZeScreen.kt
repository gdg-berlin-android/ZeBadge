package de.berlindroid.zeapp.zeui.zehome

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.berlindroid.zeapp.ROUTE_ABOUT
import de.berlindroid.zeapp.ROUTE_HOME
import de.berlindroid.zeapp.ROUTE_OPENSOURCE
import de.berlindroid.zeapp.zeui.ZeNavigationPad
import de.berlindroid.zeapp.zeui.snackbar.SnackBarData
import de.berlindroid.zeapp.zeui.snackbar.showSnackbarWithAction
import de.berlindroid.zeapp.zeui.snackbar.showSnackbarWithMessage
import de.berlindroid.zeapp.zeui.zeabout.ZeAbout
import de.berlindroid.zeapp.zeui.zeopensource.ZeOpenSource
import de.berlindroid.zeapp.zeui.zetheme.ZeBadgeAppTheme
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Composable
internal fun ZeScreen(vm: ZeBadgeViewModel, modifier: Modifier = Modifier) {
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current
    val goToReleases: () -> Unit = remember {
        {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/gdg-berlin-android/ZeBadge/releases"),
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
    val goToGithubPage: () -> Unit = remember {
        {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/gdg-berlin-android/ZeBadge"),
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navController = rememberNavController()
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: ROUTE_HOME
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarData = remember { MutableSharedFlow<SnackBarData>() }

    BackHandler(drawerState.isOpen || currentRoute != ROUTE_HOME) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            navController.navigateUp()
        }
    }

    ZeBadgeAppTheme(
        content = {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ZeDrawerContent(
                        drawerState,
                        onGetStoredPages = vm::getStoredPages,
                        onSaveAllClick = vm::saveAll,
                        onGotoReleaseClick = goToReleases,
                        onGotoContributors = {
                            if (currentRoute == ROUTE_ABOUT) {
                                navController.navigateUp()
                            } else {
                                navController.navigate(
                                    ROUTE_ABOUT,
                                )
                            }
                        },
                        onGotoOpenSourceClick = {
                            if (currentRoute == ROUTE_OPENSOURCE) {
                                navController.navigateUp()
                            } else {
                                navController.navigate(
                                    ROUTE_OPENSOURCE,
                                )
                            }
                        },
                        onUpdateConfig = vm::listConfiguration,
                        onCloseDrawer = {
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        onTitleClick = goToGithubPage,
                    )
                },
            ) {
                Scaffold(
                    modifier = modifier,
                    floatingActionButton = {
                        if (currentRoute == ROUTE_HOME) {
                            ZeNavigationPad(lazyListState)
                        }
                    },
                    topBar = {
                        ZeTopBar(
                            isNavDrawerOpen = drawerState.isOpen,
                            onOpenMenuClicked = { scope.launch { drawerState.open() } },
                            onCloseMenuClicked = { scope.launch { drawerState.close() } },
                            onTitleClick = {
                                scope.launch { drawerState.close() }
                                goToGithubPage()
                            },
                        )
                    },
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackBarHostState,
                            snackbar = {
                                Snackbar(
                                    snackbarData = it,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                        )
                    },
                ) { paddingValues ->
                    NavHost(navController = navController, startDestination = ROUTE_HOME) {
                        composable(ROUTE_HOME) {
                            ZePages(
                                paddingValues = paddingValues,
                                lazyListState = lazyListState,
                                vm = vm,
                                onShowSnackBar = { data ->
                                    scope.launch {
                                        snackBarData.emit(data)
                                    }
                                }
                            )
                        }
                        composable(ROUTE_ABOUT) {
                            ZeAbout(paddingValues)
                        }
                        composable(ROUTE_OPENSOURCE) {
                            ZeOpenSource(paddingValues)
                        }
                    }
                }
            }
        },
    )

    LaunchedEffect(Unit) {
        snackBarData.collect { data ->
            when (data) {
                is SnackBarData.SnackBarWithAction -> {
                    when (snackBarHostState.showSnackbarWithAction(data)) {
                        SnackbarResult.ActionPerformed -> {
                            data.onActionClicked.invoke()
                        }

                        SnackbarResult.Dismissed -> {
                            data.onDismissed?.invoke()
                        }
                    }
                }

                is SnackBarData.SnackBarWithMessage -> {
                    if (snackBarHostState.showSnackbarWithMessage(data) == SnackbarResult.Dismissed) {
                        data.onDismissed?.invoke()
                    }
                }
            }
        }
    }
}
