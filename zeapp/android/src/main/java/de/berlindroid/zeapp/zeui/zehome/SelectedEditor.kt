package de.berlindroid.zeapp.zeui.zehome

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeEditor
import de.berlindroid.zeapp.zemodels.ZeSlot
import de.berlindroid.zeapp.zeui.BarCodeEditorDialog
import de.berlindroid.zeapp.zeui.CustomPhraseEditorDialog
import de.berlindroid.zeapp.zeui.ImageGenerationEditorDialog
import de.berlindroid.zeapp.zeui.NameEditorDialog
import de.berlindroid.zeapp.zeui.PictureEditorDialog
import de.berlindroid.zeapp.zeui.QRCodeEditorDialog
import de.berlindroid.zeapp.zeui.RandomQuotesEditorDialog
import de.berlindroid.zeapp.zeui.WeatherEditorDialog
import de.berlindroid.zeapp.zeui.ZeCameraEditor
import de.berlindroid.zeapp.zeui.ZeImageDrawEditorDialog
import de.berlindroid.zeapp.zeui.snackbar.SnackBarData
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel
import timber.log.Timber

@Composable
internal fun SelectedEditor(
    editor: ZeEditor,
    vm: ZeBadgeViewModel,
    onShowSnackBar: (SnackBarData) -> Unit,
) {
    if (editor.slot !in listOf(
            ZeSlot.Name,
            ZeSlot.FirstCustom,
            ZeSlot.SecondCustom,
            ZeSlot.QRCode,
            ZeSlot.Weather,
            ZeSlot.Quote,
            ZeSlot.BarCode,
            ZeSlot.Add,
            ZeSlot.Camera,
        )
    ) {
        Timber.e("Slot: This slot '${editor.slot}' is not supposed to be editable.")
    } else {
        when (val config = editor.config) {
            is ZeConfiguration.Name -> NameEditorDialog(
                config = config,
                dismissed = { vm.slotConfigured(editor.slot, null) },
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
                updateMessage = vm::showMessage,
                onShowSnackBar = onShowSnackBar,
                uiAction = vm.uiAction,
            )

            is ZeConfiguration.Picture -> PictureEditorDialog(
                dismissed = { vm.slotConfigured(null, null) },
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
                updateMessage = vm::showMessage,
            )

            is ZeConfiguration.ImageGen -> ImageGenerationEditorDialog(
                initialPrompt = config.prompt,
                dismissed = { vm.slotConfigured(null, null) },
                accepted = { vm.slotConfigured(editor.slot, it) },
            )

            is ZeConfiguration.Schedule -> {
                vm.showMessage(message = stringResource(id = R.string.ze_not_added_yet_message))
                vm.slotConfigured(null, null)
            }

            is ZeConfiguration.Weather -> WeatherEditorDialog(
                config = config,
                dismissed = { vm.slotConfigured(null, null) },
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
                updateMessage = vm::showMessage,
            )

            is ZeConfiguration.Quote -> RandomQuotesEditorDialog(
                accepted = { vm.slotConfigured(editor.slot, it) },
                dismissed = { vm.slotConfigured(null, null) },
                config = config,
                updateMessage = vm::showMessage,
            )

            is ZeConfiguration.QRCode -> QRCodeEditorDialog(
                config = config,
                dismissed = { vm.slotConfigured(editor.slot, null) },
                updateMessage = vm::showMessage,
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
            )

            is ZeConfiguration.BarCode -> BarCodeEditorDialog(
                config = config,
                dismissed = { vm.slotConfigured(editor.slot, null) },
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
            )

            is ZeConfiguration.Kodee ->
                vm.slotConfigured(editor.slot, config)

            is ZeConfiguration.ImageDraw -> ZeImageDrawEditorDialog(
                dismissed = { vm.slotConfigured(editor.slot, null) },
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
            )

            is ZeConfiguration.Camera -> ZeCameraEditor(
                editor = editor,
                config = config,
                vm = vm,
            )

            is ZeConfiguration.CustomPhrase -> CustomPhraseEditorDialog(
                config = config,
                dismissed = { vm.slotConfigured(editor.slot, null) },
                udpateMessage = vm::showMessage,
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
            )
        }
    }
}
