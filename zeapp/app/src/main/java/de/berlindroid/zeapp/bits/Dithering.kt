package de.berlindroid.zeapp.bits

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.graphics.scale
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import java.nio.IntBuffer
import kotlin.math.roundToInt

fun Bitmap.invert(): Bitmap {
    val outputBitmap = grayscale()

    val buffer = IntBuffer.allocate(width * height)
    outputBitmap.copyPixelsToBuffer(buffer)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val input = buffer[x + y * width]
            val output =
                Color.rgb(255 - Color.red(input), 255 - Color.green(input), 255 - Color.blue(input))
            buffer.put(x + y * width, output)
        }
    }

    buffer.rewind()
    outputBitmap.copyPixelsFromBuffer(buffer)
    return outputBitmap
}

fun Bitmap.dither(thresholdOnly: Boolean = false, width: Int = 296, height: Int = 128): Bitmap {
    val inputImage = scale(width, height)
    val outputBitmap = inputImage.grayscale()

    val candidates = listOf(
        listOf(1, 0),
        listOf(2, 0),

        listOf(-2, 1),
        listOf(-1, 1),
        listOf(0, 1),
        listOf(1, 1),
        listOf(2, 1),

        listOf(-2, 2),
        listOf(-1, 2),
        listOf(0, 2),
        listOf(1, 2),
        listOf(2, 2),
    )
    val weight = 1.0f / candidates.size

    val buffer = IntBuffer.allocate(width * height)
    outputBitmap.copyPixelsToBuffer(buffer)

    var subindex = 0
    for (y in 0 until height) {
        for (x in 0 until width) {
            val oldGrayscale = Color.green(buffer[x + y * width])
            val newGrayscale = if (oldGrayscale < 128) 0 else 255
            val error = oldGrayscale - newGrayscale
            buffer.put(x + y * width, Color.rgb(newGrayscale, newGrayscale, newGrayscale))

            if (!thresholdOnly) {
                for ((i, j) in candidates) {
                    subindex =
                        (x + i).coerceIn(0, width - 1) + (y + j).coerceIn(0, height - 1) * width
                    val corrected =
                        (Color.green(buffer[subindex]) + error * weight).roundToInt()
                            .coerceIn(0, 255)
                    buffer.put(
                        subindex,
                        Color.rgb(corrected, corrected, corrected)
                    )
                }
            }
        }
    }

    buffer.rewind()
    outputBitmap.copyPixelsFromBuffer(buffer)
    return outputBitmap
}

fun Bitmap.grayscale(): Bitmap {
    val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outputBitmap)
    val paint = Paint()
    val colorMatrix = ColorMatrix(
        floatArrayOf(
            0.2126f, 0.7152f, 0.0722f, 0f, 0f,
            0.2126f, 0.7152f, 0.0722f, 0f, 0f,
            0.2126f, 0.7152f, 0.0722f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )

    val colorFilter = ColorMatrixColorFilter(colorMatrix)
    paint.colorFilter = colorFilter
    canvas.drawBitmap(this, 0f, 0f, paint)
    return outputBitmap
}

fun composableToBitmap(
    activity: Activity,
    content: @Composable () -> Unit,
    callback: (Bitmap) -> Unit
) {
    // create a custom view like in the good old days
    class ParentView(context: Context) : LinearLayout(context) {
        init {
            val width = PAGE_WIDTH
            val height = PAGE_HEIGHT

            val view = ComposeView(context)
            view.visibility = View.GONE
            view.layoutParams = LayoutParams(width, height)
            addView(view)

            // add the composable to make it renderable
            view.setContent {
                content()
            }

            // once it is rendered and laied out, make a bitmap
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val bitmap = createBitmapFromView(view = view, width = width, height = height)
                    callback(bitmap)
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    removeView(view)
                }
            })
        }

        private fun createBitmapFromView(view: View, width: Int, height: Int): Bitmap {
            view.layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )

            view.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )

            view.layout(0, 0, width, height)

            val canvas = Canvas()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            canvas.setBitmap(bitmap)
            view.draw(canvas)

            return bitmap
        }
    }

    // Don't look to close, nothing to see here. #handwaving
    activity.addContentView(
        ParentView(activity),
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    )
}
