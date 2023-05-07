@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package de.berlindroid.zeapp

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import de.berlindroid.zeapp.ui.CustomizeBadgeDialog
import de.berlindroid.zeapp.ui.theme.ZeBadgeAppTheme


@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZeScreen()
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
                    ZePages(this, paddingValues)
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
private fun ZePages(activity: Activity, paddingValues: PaddingValues) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(4.dp)
    ) {
        fun resetBadgeBitmap() = BitmapFactory.decodeResource(
            activity.resources,
            R.drawable.sample_badge,
        ).scale(PAGE_WIDTH, PAGE_HEIGHT)

        var name by remember { mutableStateOf("Your Name") }
        var contact by remember { mutableStateOf("Your Contact") }
        var badgeBitmap by remember { mutableStateOf(resetBadgeBitmap()) }

        var showCustomizeBadgeDialog by remember { mutableStateOf(false) }

        Column {
            if (showCustomizeBadgeDialog) {
                CustomizeBadgeDialog(
                    activity,
                    badgeBitmap,
                    name,
                    contact
                ) { newBadge, newName, newContact ->
                    badgeBitmap = newBadge
                    name = newName
                    contact = newContact

                    showCustomizeBadgeDialog = false
                }
            }

            LazyColumn {
                item {
                    PageEditor(
                        page = badgeBitmap,
                        customizeThisPage = { showCustomizeBadgeDialog = true },
                        resetThisPage = { badgeBitmap = resetBadgeBitmap() }
                    )
                }
                item {
                    PageEditor(
                        page = BitmapFactory.decodeResource(
                            activity.resources,
                            R.drawable.soon,
                        ).scale(PAGE_WIDTH, PAGE_HEIGHT),
                    )
                }
                item {
                    PageEditor(
                        page = BitmapFactory.decodeResource(
                            activity.resources,
                            R.drawable.soon,
                        ).scale(PAGE_WIDTH, PAGE_HEIGHT),
                    )
                }
                item {
                    PageEditor(
                        page = BitmapFactory.decodeResource(
                            activity.resources,
                            R.drawable.soon,
                        ).scale(PAGE_WIDTH, PAGE_HEIGHT),
                    )
                }
                item {
                    PageEditor(
                        page = BitmapFactory.decodeResource(
                            activity.resources,
                            R.drawable.soon,
                        ).scale(PAGE_WIDTH, PAGE_HEIGHT),
                    )
                }
            }
        }
    }
}

@Composable
private fun PageEditor(
    page: Bitmap,
    customizeThisPage: (() -> Unit)? = null,
    resetThisPage: (() -> Unit)? = null,
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

    if (resetThisPage != null || customizeThisPage != null) {
        Row(horizontalArrangement = Arrangement.End) {
            Spacer(modifier = Modifier.weight(1.0f))
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
