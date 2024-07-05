package de.berlindroid.zeapp.zeui.zehome

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.berlindroid.zeapp.zeui.zetheme.ZeBlack
import de.berlindroid.zeapp.zeui.zetheme.ZeWhite

@Composable
@Preview
internal fun ZeDrawerContent(
    drawerState: DrawerState = DrawerState(DrawerValue.Open),
    onCloseDrawer: () -> Unit = {},
    onTitleClick: () -> Unit = {},
    zeDrawerItems: List<ZeDrawerItemData> = listOf(),
) {
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
            items(zeDrawerItems.size) { index ->
                when (val currentItem = zeDrawerItems[index]) {
                    is ZeDrawerItemData.ItemWithText -> {
                        if (currentItem.icon != null) {
                            NavDrawerItem(
                                onClick = currentItem.onClick,
                                painter = painterResource(id = currentItem.icon),
                                text = stringResource(id = currentItem.text),
                            )
                        } else if (currentItem.imageVector != null) {
                            NavDrawerItem(
                                onClick = currentItem.onClick,
                                vector = currentItem.imageVector,
                                text = stringResource(id = currentItem.text),
                            )
                        }
                    }

                    is ZeDrawerItemData.HorizontalDivider -> {
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
                }
            }
        }
    }
}
