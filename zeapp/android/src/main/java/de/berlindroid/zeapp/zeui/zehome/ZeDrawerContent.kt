package de.berlindroid.zeapp.zeui.zehome

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zeui.zetheme.ZeBlack
import de.berlindroid.zeapp.zeui.zetheme.ZeWhite

@Composable
@Preview
internal fun ZeDrawerContent(
    drawerState: DrawerState = DrawerState(DrawerValue.Open),
    viewModel: ZeDrawerViewModel = hiltViewModel(),
    onSaveAllClick: () -> Unit = {},
    onGetStoredPages: () -> Unit = {},
    onGotoReleaseClick: () -> Unit = {},
    onGotoContributors: () -> Unit = {},
    onGotoOpenSourceClick: () -> Unit = {},
    onUpdateConfig: () -> Unit = {},
    onCloseDrawer: () -> Unit = {},
    onTitleClick: () -> Unit = {},
    newReleaseVersion: Int? = null,
) {
    val uiState by viewModel.uiState.collectAsState()

    @Composable
    fun NavDrawerItem(
        text: String,
        vector: ImageVector? = null,
        painter: Painter? = null,
        onClick: () -> Unit,
    ) {
        val shape = RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            topEnd = 30.dp,
            bottomEnd = 30.dp,
        )
        NavigationDrawerItem(
            modifier = Modifier
                .padding(
                    start = 0.dp,
                    end = 32.dp,
                    top = 4.dp,
                    bottom = 4.dp,
                )
                .border(
                    width = 1.dp,
                    color = ZeWhite,
                    shape = shape,
                ),
            colors = NavigationDrawerItemDefaults.colors(
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

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.secondary,
        drawerShape = DrawerDefaults.shape,
        modifier = Modifier
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
            item {
                NavDrawerItem(
                    onClick = onSaveAllClick,
                    painter = painterResource(id = R.drawable.save_all),
                    text = stringResource(id = R.string.ze_navdrawer_save_all_pages),
                )
            }

            item {
                NavDrawerItem(
                    text = stringResource(id = R.string.ze_navdrawer_update_config),
                    vector = Icons.Default.ThumbUp,
                    onClick = onUpdateConfig,
                )
            }

            item {
                NavDrawerItem(
                    painter = painterResource(id = R.drawable.ic_random),
                    text = stringResource(id = R.string.ze_navdrawer_send_random_page),
                    onClick = onGetStoredPages,
                )
            }

            item {
                HorizontalDivider(
                    thickness = 0.dp,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(
                        start = 0.dp,
                        end = 0.dp,
                        top = 16.dp,
                        bottom = 16.dp,
                    ),
                )
            }

            item {
                NavDrawerItem(
                    text = stringResource(id = R.string.ze_navdrawer_contributors),
                    painter = rememberVectorPainter(Icons.Default.Info),
                    onClick = onGotoContributors,
                )
            }

            item {
                NavDrawerItem(
                    text = stringResource(id = R.string.ze_navdrawer_open_source),
                    painter = painterResource(id = R.drawable.ic_open_source_initiative),
                    onClick = onGotoOpenSourceClick,
                )
            }

            item {
                HorizontalDivider(
                    thickness = 0.dp,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(
                        start = 0.dp,
                        end = 0.dp,
                        top = 16.dp,
                        bottom = 16.dp,
                    ),
                )
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
                )
            }
        }
    }
}
