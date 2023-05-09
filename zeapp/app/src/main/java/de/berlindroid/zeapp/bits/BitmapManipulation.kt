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
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import java.nio.IntBuffer
import kotlin.experimental.or
import kotlin.random.Random

/**
 * Linear invert all pixel values
 *
 * This will work best with images in black/white or grayscale.
 */
fun Bitmap.invert(): Bitmap {
    val outputBitmap = grayscale()

    val buffer = IntBuffer.allocate(width * height)
    outputBitmap.copyPixelsToBuffer(buffer)
    buffer.rewind()

    buffer.map { input ->
        Color.rgb(255 - Color.red(input), 255 - Color.green(input), 255 - Color.blue(input))
    }

    buffer.rewind()
    outputBitmap.copyPixelsFromBuffer(buffer)
    return outputBitmap
}

/**
 * Linear threshold all values above limit to white, and below to black
 *
 * @param limit the value to be considered the threshold, defaults to 128, half of the range
 */
fun Bitmap.threshold(limit: Int = 128): Bitmap {
    val outputBitmap = grayscale()

    val buffer = IntBuffer.allocate(width * height)
    outputBitmap.copyPixelsToBuffer(buffer)
    buffer.rewind()

    buffer.map { input ->
        val gray = Color.green(input)
        if (gray > limit) {
            Color.WHITE
        } else {
            Color.BLACK
        }
    }

    buffer.rewind()
    outputBitmap.copyPixelsFromBuffer(buffer)
    return outputBitmap
}

/**
 * Create new bitmap containing only the luminance values of all pixels.
 */
fun Bitmap.grayscale(): Bitmap {
    val outputBitmap = Bitmap.createBitmap(width, height, config)
    outputBitmap.density = density

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

/**
 * Return a random image
 */
fun Bitmap.randomizeColors(): Bitmap {
    val outputBitmap = copy(config, true)

    val buffer = IntBuffer.allocate(width * height)
    outputBitmap.copyPixelsToBuffer(buffer)
    buffer.rewind()

    buffer.map {
        Color.rgb(
            Random.nextInt(0, 255),
            Random.nextInt(0, 255),
            Random.nextInt(0, 255)
        )
    }

    buffer.rewind()
    outputBitmap.copyPixelsFromBuffer(buffer)
    return outputBitmap
}

/**
 * Render a composable into a bitmap.
 *
 * Warning: This will add a content view to the activity, gone, but there.
 */
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

            // once it is rendered and laid out, make a bitmap
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

/**
 * Converts a given black/white image to a binary array.
 */
fun Bitmap.toBinary(): ByteArray {
    val buffer = IntBuffer.allocate(width * height)
    copyPixelsToBuffer(buffer)
    buffer.rewind()

    val output = mutableListOf<Byte>()
    var bitIndex = 0
    var currentByte: Byte = 0
    buffer.forEachIndexed { i, pixel ->
        val value = Color.green(pixel)
        currentByte = currentByte or ((if (value == 255) 1 else 0) shl bitIndex).toByte()
        bitIndex += 1

        if (bitIndex == Byte.SIZE_BITS) {
            output.add(currentByte)
            bitIndex = 0
            currentByte = 0
        }
    }

    return output.toByteArray()
}

/**
 * Check if a given bitmap can be converted into binary form.
 *
 * The binary form consists of pixel whos color values are either all zeros or all 255.
 */
fun Bitmap.isBinary(): Boolean {
    val buffer = IntBuffer.allocate(width * height)
    copyPixelsToBuffer(buffer)
    buffer.rewind()

    var allBinaryPixel = true

    buffer.forEach { pixelColor ->
        allBinaryPixel = allBinaryPixel && pixelColor.isBinary()
    }

    return allBinaryPixel
}

private fun Int.isBinary(): Boolean {
    val r = Color.red(this)
    val g = Color.green(this)
    val b = Color.blue(this)

    return r == g && g == b && (r == 0 || r == 255)
}

/**
 * Map all values of an IntBuffer
 */
fun IntBuffer.map(mapper: (it: Int) -> Int) {
    for (i in 0 until limit()) {
        put(i, mapper(get(i)))
    }
}

/**
 * Iterate over all values of an IntBuffer
 */
fun IntBuffer.forEach(mapper: (it: Int) -> Unit) {
    for (i in 0 until limit()) {
        mapper(get(i))
    }
}

/**
 * Iterate over all values of an IntBuffer
 */
fun IntBuffer.forEachIndexed(mapper: (index: Int, it: Int) -> Unit) {
    for (i in 0 until limit()) {
        mapper(i, get(i))
    }
}
