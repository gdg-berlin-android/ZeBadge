package de.berlindroid.zeapp.zebits

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.IntBuffer
import kotlin.math.roundToInt

/**
 * Floyd-Steinberg Dithering
 *
 * Use this extension to create a binary bitmap, dithered following the Floyd-Steinberg algorithm.
 *
 * @see [Wikpedia](https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering)
 */
fun Bitmap.ditherFloydSteinberg(): Bitmap {
    val outputBitmap = grayscale()

    // create new buffer, storing only one value, it's green,
    // so the pixel color value can be temporarily bigger then 255
    val buffer = IntBuffer.allocate(width * height)
    outputBitmap.copyPixelsToBuffer(buffer)
    buffer.rewind()

    buffer.map { Color.green(it) }

    // loop through all pixels. propagating the thresholding error
    for (y in 0 until height) {
        for (x in 0 until width) {
            val old = buffer[x + y * width]
            val new = if (old < 128) 0 else 255
            val error = old - new
            buffer.put(x + y * width, new)

            // distribute the error to the neighboring pixels
            FLOYD_STEINBERG_NEIGHBOR_WEIGHTS.forEachIndexed { index, weight ->
                val i = index % 3 - 1
                val j = index / 3

                if (weight > 0.0f) {
                    if (x + i in 0 until width) {
                        if (y + j in 0 until height) {
                            val subindex = (x + i) + (y + j) * width
                            val corrected = (buffer[subindex] + error * weight).roundToInt()
                            buffer.put(subindex, corrected)
                        }
                    }
                }
            }
        }
    }

    // convert pixel values back to full color, coercing will be done by Color.rgb
    buffer.rewind()
    buffer.map { Color.rgb(it, it, it) }

    // and finally store the pixel values back into the image
    outputBitmap.copyPixelsFromBuffer(buffer)
    return outputBitmap
}

private val FLOYD_STEINBERG_NEIGHBOR_WEIGHTS = arrayOf(
    0 / 16.0f, /**/
    0.0f,
    7 / 16.0f,
    3 / 16.0f,
    5 / 16.0f,
    1 / 16.0f,
)
