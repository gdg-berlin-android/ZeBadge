package de.berlindroid.zeapp.zeui.zehome

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zeservices.github.ZeReleaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZeDrawerViewModel
    @Inject
    constructor(
        private val releaseService: ZeReleaseService,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(UiState())
        val uiState = _uiState.asStateFlow()

        init {
            checkForNewRelease()
        }

        private fun checkForNewRelease() {
            viewModelScope.launch {
                val newReleaseVersion = releaseService.getNewRelease()
                _uiState.update {
                    it.copy(
                        newReleaseVersion = newReleaseVersion,
                    )
                }
            }
        }

        data class UiState(
            val newReleaseVersion: Int? = null,
            val drawerItems: List<DrawerItem> = DrawerItemsProvider.getDrawerItems(),
        )
    }

object DrawerItemsProvider {
    fun getDrawerItems(): List<DrawerItem> =
        listOf(
            DrawerItem(
                id = DrawerItemId.ZEPASS_CHAT,
                type = DrawerItemType.NAVIGATION,
                titleRes = R.string.open_zepass_chat,
                vector = Icons.Default.Person,
            ),
            DrawerItem(
                id = DrawerItemId.ALTER_EGOS,
                type = DrawerItemType.NAVIGATION,
                titleRes = R.string.show_all_alter_egos,
                vector = Icons.Default.Star,
            ),
            DrawerItem(type = DrawerItemType.DIVIDER),
            DrawerItem(
                id = DrawerItemId.SAVE_ALL,
                type = DrawerItemType.NAVIGATION,
                titleRes = R.string.ze_navdrawer_save_all_pages,
                painter = R.drawable.save_all,
            ),
            DrawerItem(
                id = DrawerItemId.UPDATE_CONFIG,
                type = DrawerItemType.NAVIGATION,
                titleRes = R.string.ze_navdrawer_update_config,
                vector = Icons.Default.ThumbUp,
            ),
            DrawerItem(
                id = DrawerItemId.SEND_RANDOM,
                type = DrawerItemType.NAVIGATION,
                titleRes = R.string.ze_navdrawer_send_random_page,
                painter = R.drawable.ic_random,
            ),
            DrawerItem(type = DrawerItemType.DIVIDER),
            DrawerItem(
                id = DrawerItemId.SETTINGS,
                type = DrawerItemType.NAVIGATION,
                titleRes = R.string.ze_navdrawer_settings,
                painter = R.drawable.ic_settings,
            ),
            DrawerItem(type = DrawerItemType.DIVIDER),
            DrawerItem(
                id = DrawerItemId.LANGUAGE_SETTINGS,
                type = DrawerItemType.NAVIGATION,
                titleRes = R.string.ze_navdrawer_language_settings,
                painter = R.drawable.ic_language,
            ),
            DrawerItem(type = DrawerItemType.SPACE),
            DrawerItem(
                id = DrawerItemId.CONTRIBUTORS,
                type = DrawerItemType.NAVIGATION,
                titleRes = R.string.ze_navdrawer_contributors,
                vector = Icons.Default.Info,
            ),
            DrawerItem(
                id = DrawerItemId.OPEN_SOURCE,
                type = DrawerItemType.NAVIGATION,
                titleRes = R.string.ze_navdrawer_open_source,
                painter = R.drawable.ic_open_source_initiative,
            ),
            DrawerItem(type = DrawerItemType.DIVIDER),
        )
}

enum class DrawerItemType {
    NAVIGATION,
    DIVIDER,
    SPACE,
}

enum class DrawerItemId {
    ZEPASS_CHAT,
    ALTER_EGOS,
    SAVE_ALL,
    UPDATE_CONFIG,
    SEND_RANDOM,
    SETTINGS,
    LANGUAGE_SETTINGS,
    CONTRIBUTORS,
    OPEN_SOURCE,
}

data class DrawerItem(
    val id: DrawerItemId? = null,
    val type: DrawerItemType,
    val titleRes: Int = 0,
    val vector: ImageVector? = null,
    val painter: Int? = null,
)
