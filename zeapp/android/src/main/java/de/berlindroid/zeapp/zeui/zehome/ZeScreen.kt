package de.berlindroid.zeapp.zeui.zehome

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.ROUTE_ABOUT
import de.berlindroid.zeapp.ROUTE_HOME
import de.berlindroid.zeapp.ROUTE_OPENSOURCE
import de.berlindroid.zeapp.zeui.ZeNavigationPad
import de.berlindroid.zeapp.zeui.zeabout.ZeAbout
import de.berlindroid.zeapp.zeui.zeopensource.ZeOpenSource
import de.berlindroid.zeapp.zeui.zetheme.ZeBadgeAppTheme
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel
import kotlinx.coroutines.CoroutineScope
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

    ZeBadgeAppTheme(
        content = {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ZeDrawerContent(
                        drawerState,
                        onCloseDrawer = {
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        onTitleClick = goToGithubPage,
                        zeDrawerItems = getZeDrawerItems(
                            zeBadgeViewModel = vm,
                            currentRoute = currentRoute,
                            navController = navController,
                            goToReleases = goToReleases,
                        ),
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
                ) { paddingValues ->
                    NavHost(navController = navController, startDestination = ROUTE_HOME) {
                        composable(ROUTE_HOME) {
                            DrawerBackHandler(
                                drawerState = drawerState,
                                scope = scope,
                            )
                            ZePages(
                                paddingValues = paddingValues,
                                lazyListState = lazyListState,
                                vm = vm,
                            )
                        }
                        composable(ROUTE_ABOUT) {
                            DrawerBackHandler(
                                drawerState = drawerState,
                                scope = scope,
                            )
                            ZeAbout(paddingValues)
                        }
                        composable(ROUTE_OPENSOURCE) {
                            DrawerBackHandler(
                                drawerState = drawerState,
                                scope = scope,
                            )
                            ZeOpenSource(paddingValues)
                        }
                    }
                }
            }
        },
    )
}

private fun getZeDrawerItems(
    zeBadgeViewModel: ZeBadgeViewModel,
    currentRoute: String,
    navController: NavHostController,
    goToReleases: () -> Unit,
): List<ZeDrawerItemData> = listOf(
    ZeDrawerItemData.ItemWithText(
        onClick = zeBadgeViewModel::saveAll,
        icon = R.drawable.save_all,
        text = R.string.ze_navdrawer_save_all_pages,
    ),
    ZeDrawerItemData.ItemWithText(
        onClick = zeBadgeViewModel::listConfiguration,
        imageVector = Icons.Default.ThumbUp,
        text = R.string.ze_navdrawer_update_config,
    ),
    ZeDrawerItemData.ItemWithText(
        onClick = zeBadgeViewModel::listConfiguration,
        icon = R.drawable.ic_random,
        text = R.string.ze_navdrawer_send_random_page,
    ),
    ZeDrawerItemData.HorizontalDivider,
    ZeDrawerItemData.ItemWithText(
        onClick = {
            if (currentRoute == ROUTE_ABOUT) {
                navController.navigateUp()
            } else {
                navController.navigate(
                    ROUTE_ABOUT,
                )
            }
        },
        imageVector = Icons.Default.Info,
        text = R.string.ze_navdrawer_contributors,
    ),
    ZeDrawerItemData.ItemWithText(
        onClick = {
            if (currentRoute == ROUTE_OPENSOURCE) {
                navController.navigateUp()
            } else {
                navController.navigate(
                    ROUTE_OPENSOURCE,
                )
            }
        },
        icon = R.drawable.ic_open_source_initiative,
        text = R.string.ze_navdrawer_open_source,
    ),
    ZeDrawerItemData.ItemWithText(
        onClick = goToReleases,
        icon = R.drawable.ic_update,
        text = R.string.ze_navdrawer_open_release_page,
    ),
    ZeDrawerItemData.HorizontalDivider,
)

@Composable
fun DrawerBackHandler(drawerState: DrawerState, scope: CoroutineScope) {
    BackHandler(drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
}
