package de.berlindroid.zeapp.zeui.zehome

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zeui.zetheme.ZeBlack
import de.berlindroid.zeapp.zeui.zetheme.ZeWhite

@Composable
fun NavDrawerItem(
    text: String,
    vector: ImageVector? = null,
    painter: Painter? = null,
    onClick: () -> Unit,
    onCloseDrawer: () -> Unit,
) {
    val shape =
        RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            topEnd = 30.dp,
            bottomEnd = 30.dp,
        )
    NavigationDrawerItem(
        modifier =
            Modifier
                .padding(
                    start = 0.dp,
                    end = 32.dp,
                    top = 4.dp,
                    bottom = 4.dp,
                ).border(
                    width = 1.dp,
                    color = ZeWhite,
                    shape = shape,
                ),
        colors =
            NavigationDrawerItemDefaults.colors(
                unselectedTextColor = ZeWhite,
                unselectedContainerColor = ZeBlack,
            ),
        shape = shape,
        icon = {
            if (vector != null) {
                Icon(imageVector = vector, contentDescription = text, tint = ZeWhite)
            } else if (painter != null) {
                Icon(painter = painter, contentDescription = text, tint = ZeWhite)
            }
        },
        label = { Text(text = text) },
        selected = false,
        onClick = {
            onClick()
            onCloseDrawer()
        },
    )
}

@Composable
@Preview
@Suppress("LongParameterList", "LongMethod")
internal fun ZeDrawerContent(
    onSaveAllClick: () -> Unit = {},
    onGetStoredPages: () -> Unit = {},
    onGotoReleaseClick: () -> Unit = {},
    onGotoContributors: () -> Unit = {},
    onGotoLanguagesSettings: () -> Unit = {},
    onGotoOpenSourceClick: () -> Unit = {},
    onGotoZePass: () -> Unit = {},
    onGoToSettings: () -> Unit = {},
    onGoToAlterEgos: () -> Unit = {},
    onUpdateConfig: () -> Unit = {},
    onCloseDrawer: () -> Unit = {},
    onTitleClick: () -> Unit = {},
) {
    val viewModel: ZeDrawerViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Map drawer item IDs to their corresponding callback functions
    val onItemClick: (DrawerItemId?) -> Unit = { itemId ->
        when (itemId) {
            DrawerItemId.ZEPASS_CHAT -> onGotoZePass()
            DrawerItemId.ALTER_EGOS -> onGoToAlterEgos()
            DrawerItemId.SAVE_ALL -> onSaveAllClick()
            DrawerItemId.UPDATE_CONFIG -> onUpdateConfig()
            DrawerItemId.SEND_RANDOM -> onGetStoredPages()
            DrawerItemId.SETTINGS -> onGoToSettings()
            DrawerItemId.LANGUAGE_SETTINGS -> onGotoLanguagesSettings()
            DrawerItemId.CONTRIBUTORS -> onGotoContributors()
            DrawerItemId.OPEN_SOURCE -> onGotoOpenSourceClick()
            null -> {} // For dividers and spacers
        }
    }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.secondary,
        drawerShape = DrawerDefaults.shape,
        modifier =
            Modifier
                .border(
                    width = 1.dp,
                    color = ZeWhite,
                    shape = DrawerDefaults.shape,
                ),
    ) {
        ZeTitle(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 64.dp),
        ) {
            onTitleClick()
        }

        LazyColumn {
            items(uiState.drawerItems) { drawerItem ->
                when (drawerItem.type) {
                    DrawerItemType.NAVIGATION -> {
                        NavDrawerItem(
                            text = stringResource(drawerItem.titleRes),
                            vector = drawerItem.vector,
                            painter = drawerItem.painter?.let { painterResource(it) },
                            onClick = { onItemClick(drawerItem.id) },
                            onCloseDrawer = onCloseDrawer,
                        )
                    }
                    DrawerItemType.DIVIDER -> {
                        Divider()
                    }
                    DrawerItemType.SPACE -> {
                        HorizontalDivider(
                            thickness = 0.dp,
                            color = MaterialTheme.colorScheme.background,
                            modifier =
                                Modifier.padding(
                                    start = 0.dp,
                                    end = 0.dp,
                                    top = 16.dp,
                                    bottom = 16.dp,
                                ),
                        )
                    }
                }
            }

            uiState.newReleaseVersion?.let { version ->
                item {
                    Text(
                        stringResource(id = R.string.ze_navdrawer_new_release, version),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
            item {
                NavDrawerItem(
                    text = stringResource(id = R.string.ze_navdrawer_open_release_page),
                    painter = painterResource(id = R.drawable.ic_update),
                    onClick = onGotoReleaseClick,
                    onCloseDrawer = onCloseDrawer,
                )
            }
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(
        thickness = 0.dp,
        color = MaterialTheme.colorScheme.background,
        modifier =
            Modifier.padding(
                start = 0.dp,
                end = 0.dp,
                top = 16.dp,
                bottom = 16.dp,
            ),
    )
}
