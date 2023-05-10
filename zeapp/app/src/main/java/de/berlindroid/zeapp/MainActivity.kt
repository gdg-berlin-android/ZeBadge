@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package de.berlindroid.zeapp

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import de.berlindroid.zeapp.ui.BinaryBitmapPageProvider
import de.berlindroid.zeapp.ui.NameEditorDialog
import de.berlindroid.zeapp.ui.PictureEditorDialog
import de.berlindroid.zeapp.ui.ToolButton
import de.berlindroid.zeapp.ui.theme.ZeBadgeAppTheme
import de.berlindroid.zeapp.vm.BadgeViewModel
import de.berlindroid.zeapp.vm.BadgeViewModel.*
import android.content.res.Configuration as AndroidConfig
import de.berlindroid.zeapp.ui.BadgeSimulator as ZeSimulator

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    private val vm: BadgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (LocalConfiguration.current.orientation == AndroidConfig.ORIENTATION_LANDSCAPE) {
                ZeSimulator(
                    page = vm.slotToBitmap(),
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
        val editor by remember { vm.currentPageEditor }
        val templateChooser by remember { vm.currentTemplateChooser }
        val slots by remember { vm.slots }

        if (editor != null) {
            SelectedEditor(editor!!, activity, vm)
        }

        if (templateChooser != null) {
            TemplateChooserDialog(vm, templateChooser)
        }

        LazyColumn {
            items(
                listOf(
                    Slot.Name,
                    Slot.FirstSponsor,
                    Slot.SecondSponsor,
                    Slot.FirstCustom,
                    Slot.SecondCustom,
                )
            ) { slot ->
                PagePreview(
                    bitmap = vm.slotToBitmap(slot),
                    customizeThisPage = if (slot.isLocked) null else {
                        { vm.customizeSlot(slot) }
                    },
                    resetThisPage = if (slot.isLocked) null else {
                        { vm.resetSlot(slot) }
                    },
                    sendToDevice = {
                        vm.sendPageToDevice(
                            slot,
                            slots[slot]!!.bitmap
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectedEditor(
    editor: Editor,
    activity: Activity,
    vm: BadgeViewModel
) {
    if (editor.slot !in listOf(
            Slot.Name,
            Slot.FirstCustom,
            Slot.SecondCustom
        )
    ) {
        Log.e("Slot", "This slot '${editor.slot}' is not supposed to be editable.")
    } else {
        when (val config = editor.config) {
            is Configuration.Name -> NameEditorDialog(
                activity,
                config,
                dismissed = { vm.slotConfigured(editor.slot, null) }
            ) { newConfig ->
                vm.slotConfigured(editor.slot, newConfig)
            }

            is Configuration.Picture -> {
                PictureEditorDialog(activity) {
                    vm.slotConfigured(editor.slot, it)
                }
            }

            is Configuration.Schedule -> {
                Toast.makeText(
                    activity,
                    "Not added by you yet, please feel free to contribute this editor",
                    Toast.LENGTH_LONG
                ).show()

                vm.slotConfigured(null, null)
            }

            is Configuration.Weather -> {
                Toast.makeText(
                    activity,
                    "Need the weather report? Think about editing the source code!",
                    Toast.LENGTH_LONG
                ).show()

                vm.slotConfigured(null, null)
            }
        }
    }
}

@Composable
private fun TemplateChooserDialog(
    vm: BadgeViewModel,
    templateChooser: TemplateChooser?
) {
    AlertDialog(
        onDismissRequest = {
            vm.templateSelected(null, null)
        },
        confirmButton = {
            Button(onClick = { vm.templateSelected(null, null) }) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        title = {
            Text(text = "Select Content")
        },
        text = {
            LazyColumn {
                items(templateChooser?.configurations.orEmpty()) { config ->
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            vm.templateSelected(templateChooser?.slot, config)
                        },
                    ) {
                        Text(text = config.humanTitle)
                    }
                }
            }
        }
    )
}

@Composable
@Preview
private fun PagePreview(
    @PreviewParameter(BinaryBitmapPageProvider::class, 1)
    bitmap: Bitmap,
    customizeThisPage: (() -> Unit)? = null,
    resetThisPage: (() -> Unit)? = null,
    sendToDevice: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier
            .background(Color.Black, RoundedCornerShape(8.dp))
            .padding(2.dp),
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(unbounded = true)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            painter = BitmapPainter(
                image = bitmap.asImageBitmap(),
                filterQuality = FilterQuality.None,
            ),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
        )

        if (resetThisPage != null || customizeThisPage != null || sendToDevice != null) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (sendToDevice != null) {
                    item {
                        ToolButton(
                            imageVector = Icons.Filled.Send,
                            text = "Send",
                            onClick = sendToDevice,
                        )
                    }
                }
                if (resetThisPage != null) {
                    item {
                        ToolButton(
                            imageVector = Icons.Filled.Refresh,
                            text = "Reset",
                            onClick = resetThisPage,
                        )
                    }
                }
                if (customizeThisPage != null) {
                    item {
                        ToolButton(
                            imageVector = Icons.Filled.Edit,
                            text = "Change",
                            onClick = customizeThisPage,
                        )
                    }
                }
            }
        }
    }
}

private val Slot.isLocked: Boolean
    get() = this is Slot.FirstSponsor || this is Slot.SecondSponsor
