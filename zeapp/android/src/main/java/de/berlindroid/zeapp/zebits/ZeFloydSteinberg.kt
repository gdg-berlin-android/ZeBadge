package de.berlindroid.zeapp.zebits

import java.nio.IntBuffer
import kotlin.math.roundToInt

/**
 * Floyd-Steinberg Dithering
 *
 * Use this extension to create a binary pixel buffers, dithered following the Floyd-Steinberg algorithm.
 * @see @https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
 *
 * @param width the amount of horizontal pixels stored in buffer
 * @param height the amount of pixels stored vertically
 */
fun IntBuffer.ditherFloydSteinberg(width: Int, height: Int): IntBuffer {
    // grayscale and single value the pixels
    val output = grayscale()
    output.map {
        it.green()
    }

    // loop through all pixels. propagating the thresholding error
    for (y in 0 until height) {
        for (x in 0 until width) {
            val old = output[x + y * width]
            val new = if (old < 128) 0 else 255
            val error = old - new
            output.put(x + y * width, new)

            // distribute the error to the neighboring pixels
            FLOYD_STEINBERG_NEIGHBOR_WEIGHTS.forEachIndexed { index, weight ->
                val i = index % 3 - 1
                val j = index / 3

                if (weight > 0.0f) {
                    if (x + i in 0 until width) {
                        if (y + j in 0 until height) {
                            val subindex = (x + i) + (y + j) * width
                            val corrected = (output[subindex] + error * weight).roundToInt()
                            output.put(subindex, corrected)
                        }
                    }
                }
            }
        }
    }

    // convert pixel values back to "full color"
    rewind()

    output.map { rgb(it, it, it) }
    output.rewind()

    return output
}

private val FLOYD_STEINBERG_NEIGHBOR_WEIGHTS = arrayOf(
    0 / 16.0f, /**/
    0.0f,
    7 / 16.0f,
    3 / 16.0f,
    5 / 16.0f,
    1 / 16.0f,
)
