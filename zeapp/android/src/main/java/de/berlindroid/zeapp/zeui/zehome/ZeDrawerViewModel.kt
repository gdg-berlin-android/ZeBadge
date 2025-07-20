package de.berlindroid.zeapp.zeui.zehome

import androidx.compose.material.icons.Icons
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
            // Version of a new release, in case there is one
            val newReleaseVersion: Int? = null,
            val drawerItems: List<DrawerItem> = getDefaultDrawerItems(),
        )

        companion object {
            fun getDefaultDrawerItems(): List<DrawerItem> = listOf(
                DrawerItem(
                    type = DrawerItemType.NAVIGATION,
                    titleRes = R.string.open_zepass_chat,
                    vector = Icons.Default.Person,
                    onClick = {}
                ),
                DrawerItem(
                    type = DrawerItemType.NAVIGATION,
                    titleRes = R.string.show_all_alter_egos,
                    vector = Icons.Default.Star,
                    onClick = {}
                ),
                DrawerItem(type = DrawerItemType.DIVIDER),
                DrawerItem(
                    type = DrawerItemType.NAVIGATION,
                    titleRes = R.string.ze_navdrawer_save_all_pages,
                    painter = R.drawable.save_all,
                    onClick = {}
                ),
                DrawerItem(
                    type = DrawerItemType.NAVIGATION,
                    titleRes = R.string.ze_navdrawer_update_config,
                    vector = Icons.Default.ThumbUp,
                    onClick = {}
                ),
                DrawerItem(
                    type = DrawerItemType.NAVIGATION,
                    titleRes = R.string.ze_navdrawer_send_random_page,
                    painter = R.drawable.ic_random,
                    onClick = {}
                ),
                DrawerItem(type = DrawerItemType.DIVIDER),
                DrawerItem(
                    type = DrawerItemType.NAVIGATION,
                    titleRes = R.string.ze_navdrawer_settings,
                    painter = R.drawable.ic_settings,
                    onClick = {}
                ),
                DrawerItem(type = DrawerItemType.DIVIDER),
                DrawerItem(
                    type = DrawerItemType.NAVIGATION,
                    titleRes = R.string.ze_navdrawer_language_settings,
                    painter = R.drawable.ic_language,
                    onClick = {}
                ),
                DrawerItem(type = DrawerItemType.SPACE),
                DrawerItem(
                    type = DrawerItemType.NAVIGATION,
                    titleRes = R.string.ze_navdrawer_contributors,
                    painter = R.drawable.ic_info,
                    onClick = {}
                ),
                DrawerItem(
                    type = DrawerItemType.NAVIGATION,
                    titleRes = R.string.ze_navdrawer_open_source,
                    painter = R.drawable.ic_open_source_initiative,
                    onClick = {}
                ),
                DrawerItem(type = DrawerItemType.DIVIDER),
            )
        }
    }

enum class DrawerItemType {
    NAVIGATION,
    DIVIDER,
    SPACE
}

data class DrawerItem(
    val type: DrawerItemType,
    val titleRes: Int = 0,
    val vector: ImageVector? = null,
    val painter: Int? = null,
    val onClick: () -> Unit = {}
)
    }
