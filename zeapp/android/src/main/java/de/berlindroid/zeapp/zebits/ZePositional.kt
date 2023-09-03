package de.berlindroid.zeapp.zebits

import java.nio.IntBuffer
import kotlin.math.roundToInt

/**
 * Attempt on a static positional pattern dithering algorithm.
 *
 * This will convert a color bitmap into a black and white image following a set amount of static
 * positional 3x3 patterns.
 */
fun IntBuffer.ditherPositional(width: Int, height: Int): IntBuffer {
    val output = grayscale()
    output.rewind()

    output.map { it.green() }

    // loop through all pixels. propagating the thresholding to specific pixel only error
    for (y in 0 until height) {
        for (x in 0 until width) {
            val old = output[x + y * width]
            val new = if (old < 128) 0 else 255
            val error = old - new
            output.put(x + y * width, new)

            // select weight according to the index of the pixel
            val weights = if ((x + y) % 2 == 0) EVEN_NEIGHBOR_WEIGHTS else ODD_NEIGHBOR_WEIGHTS
            weights.forEachIndexed { index, weight ->
                val i = index % 2 - 1
                val j = index / 2

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

    // convert pixel values back to full color, coercing will be done by Color.rgb
    output.map { rgb(it, it, it) }
    output.rewind()

    return output
}

private val EVEN_NEIGHBOR_WEIGHTS = arrayOf(
    0 / 4.0f, /**/
    0.0f,
    0 / 4.0f,
    2 / 4.0f,
    0 / 4.0f,
    2 / 4.0f,
)

private val ODD_NEIGHBOR_WEIGHTS = arrayOf(
    0 / 4.0f, /**/
    0.0f,
    2 / 4.0f,
    0 / 4.0f,
    2 / 4.0f,
    0 / 4.0f,
)
