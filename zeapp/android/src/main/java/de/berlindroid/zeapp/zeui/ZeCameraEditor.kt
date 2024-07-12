package de.berlindroid.zeapp.zeui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import de.berlindroid.zeapp.BuildConfig
import de.berlindroid.zeapp.zebits.toDitheredImage
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeEditor
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ZeCameraEditor(
    editor: ZeEditor,
    config: ZeConfiguration.Camera,
    vm: ZeBadgeViewModel,
) {
    val context = LocalContext.current
    val uri =
        FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.files",
            File(context.cacheDir, "photo.jpg"),
        )
    val coroutineScope = rememberCoroutineScope()
    val takePicture =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { pictureTaken ->
            if (pictureTaken) {
                coroutineScope.launch {
                    val bitmap = uri.toDitheredImage(context)
                    vm.slotConfigured(
                        editor.slot,
                        config.copy(bitmap = bitmap),
                    )
                }
            } else {
                vm.slotConfigured(editor.slot, null)
            }
        }

    SideEffect {
        takePicture.launch(uri)
    }
}
