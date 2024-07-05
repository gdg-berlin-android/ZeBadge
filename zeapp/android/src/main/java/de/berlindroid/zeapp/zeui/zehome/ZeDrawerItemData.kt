package de.berlindroid.zeapp.zeui.zehome

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ZeDrawerItemData {

    data class ItemWithText(
        val onClick: () -> Unit,
        @DrawableRes val icon: Int? = null,
        val imageVector: ImageVector? = null,
        @StringRes val text: Int,
    ) : ZeDrawerItemData()

    data object HorizontalDivider : ZeDrawerItemData()
}
