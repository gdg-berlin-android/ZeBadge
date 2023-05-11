@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package de.berlindroid.zeapp

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.berlindroid.zeapp.ui.CustomizeBadgeDialog
import de.berlindroid.zeapp.ui.theme.ZeBadgeAppTheme
import de.berlindroid.zeapp.vm.BadgeViewModel
import de.berlindroid.zeapp.ui.BadgeSimulator as ZeSimulator


@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    val vm: BadgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ZeSimulator(
                    page = vm.currentPageCharToPageBitmap(),
                    onButtonPressed = vm::simulatorButtonPressed,
                )
            } else {
                ZeScreen()
            }
        }
    }

    @Composable
    private fun ZeScreen() {
        ZeBadgeAppTheme(content = {
            Scaffold(
                topBar = {
                    ZeTopBar()
                },
                content = { paddingValues ->
                    ZePages(this, paddingValues, vm)
                }
            )
        })
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ZeTopBar() {
    TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
    )
}


@Composable
private fun ZePages(activity: Activity, paddingValues: PaddingValues, vm: BadgeViewModel) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(4.dp)
    ) {

        var name by remember { vm.name }
        var contact by remember { vm.contact }
        var badgePageBitmap by remember { vm.namePage }
        var firstSponsorPageBitmap by remember { vm.firstSponsorPage }
        var secondSponsorPageBitmap by remember { vm.secondSponsorPage }
        var firstCustomPageBitmap by remember { vm.firstCustomPage }
        var secondCustomPageBitmap by remember { vm.secondCustomPage }

        var showNameEditorDialog by remember { vm.nameEditorDialog }

        Column {
            if (showNameEditorDialog) {
                CustomizeBadgeDialog(
                    activity,
                    badgePageBitmap,
                    name,
                    contact
                ) { newBadge, newName, newContact ->
                    badgePageBitmap = newBadge
                    name = newName
                    contact = newContact

                    showNameEditorDialog = false
                }
            }

            LazyColumn {
                item {
                    PageEditor(
                        page = badgePageBitmap,
                        customizeThisPage = { showNameEditorDialog = true },
                        resetThisPage = { vm.resetNamePage() },
                        sendToDevice = { vm.sendPageToDevice("blink", badgePageBitmap) } // TODO: Mario – blink works, others not yet
                    )
                }

                item { PageEditor(page = firstSponsorPageBitmap) }

                item { PageEditor(page = secondSponsorPageBitmap) }

                item { PageEditor(page = firstCustomPageBitmap) }

                item { PageEditor(page = secondCustomPageBitmap) }
            }
        }
    }
}

@Composable
private fun PageEditor(
    page: Bitmap,
    customizeThisPage: (() -> Unit)? = null,
    resetThisPage: (() -> Unit)? = null,
    sendToDevice: (() -> Unit)? = null,
) {
    Image(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(unbounded = true)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        painter = BitmapPainter(
            image = page.asImageBitmap(),
            filterQuality = FilterQuality.None,
        ),
        contentScale = ContentScale.FillWidth,
        contentDescription = null,
    )

    if (resetThisPage != null || customizeThisPage != null || sendToDevice != null) {
        Row(horizontalArrangement = Arrangement.End) {
            Spacer(modifier = Modifier.weight(1.0f))
            if (sendToDevice != null) {
                IconButton(
                    modifier = Modifier.padding(horizontal = 2.dp),
                    onClick = sendToDevice
                ) { Icon(imageVector = Icons.Filled.Send, contentDescription = "send to badge") }
            }
            if (resetThisPage != null) {
                IconButton(
                    modifier = Modifier.padding(horizontal = 2.dp),
                    onClick = resetThisPage,
                ) { Icon(imageVector = Icons.Filled.Refresh, contentDescription = "reset") }
            }
            if (customizeThisPage != null) {
                IconButton(
                    modifier = Modifier.padding(horizontal = 2.dp),
                    onClick = customizeThisPage
                ) { Icon(imageVector = Icons.Filled.Edit, contentDescription = "edit") }
            }
        }
    }
}
