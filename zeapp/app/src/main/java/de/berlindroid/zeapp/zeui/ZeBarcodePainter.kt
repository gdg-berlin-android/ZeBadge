package de.berlindroid.zeapp.zeui

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.oned.Code128Writer

@Composable
fun rememberBarcodeBitmapPainter(
    content: String,
    size: Dp = 140.dp,
    padding: Dp = 0.dp,
    widthSize: Dp = 220.dp,
): BitmapPainter {
    val density = LocalDensity.current
    val sizePx = with(density) { size.roundToPx() }
    val paddingPx = with(density) { padding.roundToPx() }
    val widthSizePx = with(density) { widthSize.roundToPx() }

    var bitmap by remember(content) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(bitmap) {
        if (bitmap != null) return@LaunchedEffect

        val code128Writer = Code128Writer()

        val encodeHints = mutableMapOf<EncodeHintType, Any?>()
            .apply {
                this[EncodeHintType.MARGIN] = paddingPx
            }

        val bitmapMatrix = try {
            code128Writer.encode(
                content,
                BarcodeFormat.CODE_128,
                widthSizePx,
                sizePx,
                encodeHints,
            )
        } catch (ex: WriterException) {
            null
        }

        val matrixWidth = bitmapMatrix?.width ?: widthSizePx
        val matrixHeight = bitmapMatrix?.height ?: sizePx

        val newBitmap = Bitmap.createBitmap(
            bitmapMatrix?.width ?: widthSizePx,
            bitmapMatrix?.height ?: sizePx,
            Bitmap.Config.ARGB_8888,
        )

        for (x in 0 until matrixWidth) {
            for (y in 0 until matrixHeight) {
                val shouldColorPixel = bitmapMatrix?.get(x, y) ?: false
                val pixelColor = if (shouldColorPixel) Color.BLACK else Color.WHITE

                newBitmap.setPixel(x, y, pixelColor)
            }
        }

        bitmap = newBitmap
    }

    return remember(bitmap) {
        val currentBitmap = bitmap ?: Bitmap.createBitmap(
            sizePx,
            sizePx,
            Bitmap.Config.ARGB_8888,
        ).apply { eraseColor(Color.TRANSPARENT) }

        BitmapPainter(currentBitmap.asImageBitmap())
    }
}
