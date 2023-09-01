package de.berlindroid.zeapp.zeui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.commit451.coiltransformations.CropTransformation
import de.berlindroid.zeapp.BuildConfig
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.zebits.ditherFloydSteinberg
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
    val uri = FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.files",
        File(context.cacheDir, "photo.jpg"),
    )
    val coroutineScope = rememberCoroutineScope()
    val takePicture =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { pictureTaken ->
            if (pictureTaken) {
                val imageRequest = ImageRequest.Builder(context)
                    .data(uri)
                    .transformations(CropTransformation())
                    .size(PAGE_WIDTH, PAGE_HEIGHT)
                    .scale(Scale.FIT)
                    .precision(Precision.EXACT)
                    .allowHardware(false)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .build()

                coroutineScope.launch {
                    val drawable =
                        context.imageLoader.execute(imageRequest).drawable as BitmapDrawable
                    val bitmap = Bitmap.createBitmap(
                        PAGE_WIDTH,
                        PAGE_HEIGHT,
                        Bitmap.Config.ARGB_8888,
                    )
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(
                        drawable.bitmap,
                        (PAGE_WIDTH / 2f) - (drawable.bitmap.width / 2f),
                        0f,
                        null,
                    )
                    vm.slotConfigured(
                        editor.slot,
                        config.copy(bitmap = bitmap.ditherFloydSteinberg()),
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
