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
import androidx.compose.foundation.Image as ZeImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement as ZeArrangement
import androidx.compose.foundation.layout.Column as ZeColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row as ZeRow
import androidx.compose.foundation.layout.Spacer as ZeSpacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn as ZeLazyColumn
import androidx.compose.foundation.lazy.LazyRow as ZeLazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape as ZeRoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog as ZeAlertDialog
import androidx.compose.material3.Button as ZeButton
import androidx.compose.material3.Card as ZeCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator as ZeLinearProgressIndicator
import androidx.compose.material3.Icon as ZeIcon
import androidx.compose.material3.Scaffold as ZeScaffold
import androidx.compose.material3.Surface as ZeSurface
import androidx.compose.material3.Text as ZeText
import androidx.compose.material3.TopAppBar as ZeTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment as ZeAlignment
import androidx.compose.ui.Modifier as ZeModifier
import androidx.compose.ui.graphics.Color as ZeColor
import androidx.compose.ui.graphics.FilterQuality as ZeFilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter as ZeBitmapPainter
import androidx.compose.ui.layout.ContentScale as ZeContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.berlindroid.zeapp.zeui.BinaryBitmapPageProvider
import de.berlindroid.zeapp.zeui.ImageGenerationEditorDialog
import de.berlindroid.zeapp.zeui.NameEditorDialog
import de.berlindroid.zeapp.zeui.PictureEditorDialog
import de.berlindroid.zeapp.zeui.QRCodeEditorDialog
import de.berlindroid.zeapp.zeui.ToolButton as ZeToolButton
import de.berlindroid.zeapp.zeui.zetheme.ZeBadgeAppTheme
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel.*
import android.content.res.Configuration as AndroidConfig
import androidx.compose.material3.IconButton as ZeIconButton
import de.berlindroid.zeapp.zeui.BadgeSimulator as ZeSimulator

/**
 * Main View entrance for the app
 */
@ExperimentalMaterial3Api
class ZeMainActivity : ComponentActivity() {
    private val vm: ZeBadgeViewModel by viewModels()

    /**
     * Once created, use the main view composables.
     */
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
            ZeScaffold(
                topBar = {
                    ZeTopBar(vm)
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
private fun ZeTopBar(vm: ZeBadgeViewModel) {
    ZeTopAppBar(
        title = { ZeText(stringResource(id = R.string.app_name)) },
        actions = {
            ZeIconButton(onClick = { vm.sendRandomPageToDevice() }) {
                ZeIcon(
                    painter = painterResource(id = R.drawable.ic_random),
                    contentDescription = "Send random page to badge"
                )
            }
            ZeIconButton(onClick = { vm.saveAll() }) {
                ZeIcon(
                    painter = painterResource(id = R.drawable.save_all),
                    contentDescription = null
                )
            }
        }
    )
}


@Composable
private fun ZePages(activity: Activity, paddingValues: PaddingValues, vm: ZeBadgeViewModel) {
    ZeSurface(
        modifier = ZeModifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(4.dp)
    ) {
        val editor by remember { vm.currentSlotEditor }
        val templateChooser by remember { vm.currentTemplateChooser }
        val message by remember { vm.message }
        val messageProgress by remember { vm.messageProgress }
        val slots by remember { vm.slots }

        if (editor != null) {
            SelectedEditor(editor!!, activity, vm)
        }

        if (templateChooser != null) {
            TemplateChooserDialog(vm, templateChooser)
        }

        // column surrounding a lazycolumn: so the message stays ontop.
        ZeColumn {
            if (message.isNotEmpty()) {
                InfoBar(message, messageProgress, vm::copyInfoToClipboard)
            }
            ZeLazyColumn(
                contentPadding = PaddingValues(
                    horizontal = 8.dp,
                    vertical = 4.dp
                )
            ) {
                items(
                    slots.keys.toList()
                ) { slot ->
                    PagePreview(
                        bitmap = vm.slotToBitmap(slot),
                        customizeThisPage = if (slot.isSponsor) {
                            { vm.customizeSponsorSlot(slot) }
                        } else {
                            { vm.customizeSlot(slot) }
                        },
                        resetThisPage = if (slot.isSponsor) null else {
                            { vm.resetSlot(slot) }
                        },
                        sendToDevice = {
                            vm.sendPageToDevice(slot)
                        }
                    )

                    ZeSpacer(modifier = ZeModifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Show a message to the user with a timer ticking down.
 */
@Composable
@Preview
private fun InfoBar(
    message: String = "Very Important",
    progress: Float = 0.5f,
    copyMoreToClipboard: (() -> Unit) = {},
) {
    ZeCard(
        modifier = ZeModifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .background(ZeColor.Black, ZeRoundedCornerShape(8.dp)),
    ) {
        ZeRow(
            modifier = ZeModifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = ZeAlignment.CenterVertically
        ) {
            ZeText(
                modifier = ZeModifier.weight(1.0f),
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                color = ZeColor.White,
                text = message,
            )

            ZeIconButton(onClick = copyMoreToClipboard) {
                ZeIcon(
                    painter = painterResource(
                        id = R.drawable.copy_clipboard
                    ),
                    contentDescription = null
                )
            }
        }

        ZeLinearProgressIndicator(
            modifier = ZeModifier.fillMaxWidth(),
            progress = progress
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedEditor(
    editor: Editor,
    activity: Activity,
    vm: ZeBadgeViewModel
) {
    if (editor.slot !in listOf(
            Slot.Name,
            Slot.FirstCustom,
            Slot.SecondCustom,
            Slot.QRCode
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
                PictureEditorDialog(
                    dismissed = {
                        vm.slotConfigured(null, null)
                    }
                ) {
                    vm.slotConfigured(editor.slot, it)
                }
            }

            is Configuration.ImageGen -> {
                ImageGenerationEditorDialog(
                    config.prompt,
                    dismissed = {
                        vm.slotConfigured(null, null)
                    }
                ) {
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

            is Configuration.QRCode -> QRCodeEditorDialog(
                activity,
                config,
                dismissed = { vm.slotConfigured(editor.slot, null) }
            ) { newConfig ->
                vm.slotConfigured(editor.slot, newConfig)
            }
        }
    }
}

@Composable
private fun TemplateChooserDialog(
    vm: ZeBadgeViewModel,
    templateChooser: TemplateChooser?
) {
    ZeAlertDialog(
        onDismissRequest = {
            vm.templateSelected(null, null)
        },
        confirmButton = {
            ZeButton(onClick = { vm.templateSelected(null, null) }) {
                ZeText(text = stringResource(id = android.R.string.ok))
            }
        },
        title = {
            ZeText(text = "Select Content")
        },
        text = {
            ZeLazyColumn {
                items(templateChooser?.configurations.orEmpty()) { config ->
                    ZeButton(
                        modifier = ZeModifier.fillMaxWidth(),
                        onClick = {
                            vm.templateSelected(templateChooser?.slot, config)
                        },
                    ) {
                        ZeText(text = config.humanTitle)
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
    ZeCard(
        modifier = ZeModifier
            .background(ZeColor.Black, ZeRoundedCornerShape(8.dp))
            .padding(2.dp),
    ) {
        ZeImage(
            modifier = ZeModifier
                .fillMaxWidth()
                .wrapContentHeight(unbounded = true)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            painter = ZeBitmapPainter(
                image = bitmap.asImageBitmap(),
                filterQuality = ZeFilterQuality.None,
            ),
            contentScale = ZeContentScale.FillWidth,
            contentDescription = null,
        )

        if (resetThisPage != null || customizeThisPage != null || sendToDevice != null) {
            ZeLazyRow(
                modifier = ZeModifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 2.dp),
                horizontalArrangement = ZeArrangement.End
            ) {
                if (sendToDevice != null) {
                    item {
                        ZeToolButton(
                            imageVector = Icons.Filled.Send,
                            text = "Send",
                            onClick = sendToDevice,
                        )
                    }
                }
                if (resetThisPage != null) {
                    item {
                        ZeToolButton(
                            imageVector = Icons.Filled.Refresh,
                            text = "Reset",
                            onClick = resetThisPage,
                        )
                    }
                }
                if (customizeThisPage != null) {
                    item {
                        ZeToolButton(
                            imageVector = Icons.Filled.Edit,
                            text = "Edit",
                            onClick = customizeThisPage,
                        )
                    }
                }
            }
        }
    }
}

private val Slot.isSponsor: Boolean
    get() = this is Slot.FirstSponsor || this is Slot.SecondSponsor
