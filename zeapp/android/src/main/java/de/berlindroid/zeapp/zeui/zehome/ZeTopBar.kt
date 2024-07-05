package de.berlindroid.zeapp.zeui.zehome

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ZeTopBar(
    isNavDrawerOpen: Boolean,
    onOpenMenuClicked: () -> Unit,
    onCloseMenuClicked: () -> Unit,
    onTitleClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = if (isNavDrawerOpen) onCloseMenuClicked else onOpenMenuClicked) {
                Icon(
                    imageVector = if (isNavDrawerOpen) {
                        Icons.AutoMirrored.Filled.ArrowBack
                    } else {
                        Icons.Filled.Menu
                    },
                    contentDescription = "Menu button",
                )
            }
        },
        title = {
            ZeTitle {
                onTitleClick()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            titleContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}
