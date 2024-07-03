package de.berlindroid.zeapp.zebits

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.graphics.scale
import de.berlindroid.zekompanion.BADGE_HEIGHT
import de.berlindroid.zekompanion.BADGE_WIDTH
import de.berlindroid.zeapp.zeui.zepages.BarCodePage
import de.berlindroid.zeapp.zeui.zepages.QRCodePage
import de.berlindroid.zekompanion.forEach
import de.berlindroid.zekompanion.forEachIndexed
import de.berlindroid.zekompanion.isBinary
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.IntBuffer
import kotlin.experimental.and

/**
 * Render a composable into a bitmap.
 *
 * Warning: This will add a content view to the activity, gone, but there.
 */
fun composableToBitmap(
    activity: Activity,
    content: @Composable () -> Unit,
    callback: (Bitmap) -> Unit,
) {
    // create a custom view like in the good old days
    class ParentView(context: Context) : LinearLayout(context) {
        init {
            val width = BADGE_WIDTH
            val height = BADGE_HEIGHT

            val view = ComposeView(context)
            view.visibility = View.GONE
            view.layoutParams = LayoutParams(width, height)
            addView(view)

            // add the composable to make it renderable
            view.setContent {
                content()
            }

            // once it is rendered and laid out, make a bitmap
            viewTreeObserver.addOnGlobalLayoutListener(
                object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        val bitmap = createBitmapFromView(view = view, width = width, height = height)
                        callback(bitmap)
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        removeView(view)
                    }
                },
            )
        }

        private fun createBitmapFromView(view: View, width: Int, height: Int): Bitmap {
            view.layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
            )

            view.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
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
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ),
    )
}

fun qrComposableToBitmap(
    activity: Activity,
    title: String,
    text: String,
    qrContent: String,
    callback: (Bitmap) -> Unit,
) {
    // create a custom view like in the good old days
    class ParentView(context: Context) : LinearLayout(context) {
        init {
            val width = BADGE_WIDTH
            val height = BADGE_HEIGHT

            val view = ComposeView(context)
            view.visibility = View.GONE
            view.layoutParams = LayoutParams(width, height)
            addView(view)

            // add the composable to make it renderable
            view.setContent {
                QRCodePage(title, text, qrContent)
            }

            // once it is rendered and laid out, make a bitmap
            viewTreeObserver.addOnGlobalLayoutListener(
                object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        val bitmap = createBitmapFromView(view = view, width = width, height = height)
                        callback(bitmap)
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        removeView(view)
                    }
                },
            )
        }

        private fun createBitmapFromView(view: View, width: Int, height: Int): Bitmap {
            view.layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
            )

            view.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
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
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ),
    )
}

fun barCodeComposableToBitmap(
    activity: Activity,
    title: String,
    url: String,
    callback: (Bitmap) -> Unit,
) {
    // create a custom view like in the good old days
    class ParentView(context: Context) : LinearLayout(context) {
        init {
            val width = BADGE_WIDTH
            val height = BADGE_HEIGHT

            val view = ComposeView(context)
            view.visibility = View.GONE
            view.layoutParams = LayoutParams(width, height)
            addView(view)

            // add the composable to make it renderable
            view.setContent {
                BarCodePage(title, url)
            }

            // once it is rendered and laid out, make a bitmap
            viewTreeObserver.addOnGlobalLayoutListener(
                object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        val bitmap = createBitmapFromView(view = view, width = width, height = height)
                        callback(bitmap)
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        removeView(view)
                    }
                },
            )
        }

        private fun createBitmapFromView(view: View, width: Int, height: Int): Bitmap {
            view.layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
            )

            view.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
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
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ),
    )
}

/**
 * Converts a given binary byte buffer to a bitmap.
 *
 * Every pixel corresponds to one bit in the byte buffer: 1 means white, 0 means black.
 */
fun ByteBuffer.toBitmap(width: Int, height: Int): Bitmap {
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val buffer = IntBuffer.allocate(width * height)

    // loop through all bytes
    forEach { byte ->
        // loop through all bits
        (0 until Byte.SIZE_BITS).forEach { bitNumber ->
            // have we found a byte whose bit at the current position is not null?
            // aka is the current byte null?
            val bitOnlyByte = byte and (1 shl (7 - bitNumber)).toByte()
            val color = if (bitOnlyByte == 0.toByte()) {
                Color.BLACK
            } else {
                Color.WHITE
            }

            buffer.put(color)
        }
    }

    buffer.rewind()
    output.copyPixelsFromBuffer(buffer)

    return output
}

/**
 * Check if a given bitmap is in binary form.
 *
 * The binary form consists of pixel whos color values are either all zeros or all 255.
 */
fun Bitmap.isBinary(): Boolean {
    val buffer = IntBuffer.allocate(width * height)
    copyPixelsToBuffer(buffer)
    buffer.rewind()

    var allBinaryPixel = true

    buffer.forEachIndexed(
        exitIf = { !allBinaryPixel },
    ) { index, pixelColor ->
        val binary = pixelColor.isBinary()
        if (!binary) {
            val x = index % width
            val y = index / height

            Timber.d("Binary Editor: Pixel nr $index at $x, $y is not binary!")
        }

        allBinaryPixel = allBinaryPixel && binary
    }

    return allBinaryPixel
}

/**
 * Take this bitmap and crop out a page from it's center.
 *
 * The width will be scaled to BADGE_WIDTH, but the height will be cropped.
 */
fun Bitmap.cropPageFromCenter(): Bitmap {
    val aspectRatio = this.width.toFloat().div(this.height.toFloat())
    val targetHeight = BADGE_WIDTH.div(aspectRatio).toInt()

    return scale(BADGE_WIDTH, targetHeight)
        .crop(
            fromX = 0,
            fromY = BADGE_HEIGHT / 2 - targetHeight / 2,
            targetWidth = BADGE_WIDTH,
            targetHeight = BADGE_HEIGHT,
        )
}

/**
 * Copy the bitmap, keeping its config and make it modifiable
 */
fun Bitmap.copy(): Bitmap = copy(config, true)

/**
 * Only scale an image if it is needed, otherwise return a copy.
 */
fun Bitmap.scaleIfNeeded(targetWidth: Int, targetHeight: Int): Bitmap =
    if (width != targetWidth || height != targetHeight) {
        scale(targetWidth, targetHeight)
    } else {
        copy()
    }

private fun Bitmap.crop(fromX: Int, fromY: Int, targetWidth: Int, targetHeight: Int): Bitmap {
    val result = Bitmap.createBitmap(targetWidth, targetHeight, config)
    val canvas = Canvas(result)
    canvas.drawBitmap(this, fromX.toFloat(), fromY.toFloat(), null)
    return result
}
