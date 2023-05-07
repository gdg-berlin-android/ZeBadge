package de.berlindroid.zeapp.bits

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.IntBuffer
import kotlin.math.abs

/**
 * Dither using a static pattern
 *
 * This dithering results in higher pixel error rate. It compares any PATTERN_SIZExPATTERN_SIZE pixel values with a given
 * set of PATTERN_SIZExPATTERN_SIZE patterns, and selects the one with the least pixel error.
 */
fun Bitmap.ditherStaticPattern(): Bitmap {
    val outputBitmap = grayscale()

    val buffer = IntBuffer.allocate(width * height)
    outputBitmap.copyPixelsToBuffer(buffer)
    buffer.rewind()

    buffer.map { Color.green(it) }

    // loop through pixels, one block at a time
    for (blockY in 0 until height / PATTERN_SIZE) {
        for (blockX in 0 until width / PATTERN_SIZE) {
            // find block with minimal error
            val min = PATTERNS.minBy { pattern ->
                pattern.differenceToBlock(buffer, blockX, blockY, width, height)
            }

            // put minimal error block in image
            for (y in 0 until PATTERN_SIZE) {
                for (x in 0 until PATTERN_SIZE) {
                    val bufferX = (blockX * PATTERN_SIZE + x).coerceIn(0, width - 1)
                    val bufferY = (blockY * PATTERN_SIZE + y).coerceIn(0, height - 1)

                    buffer.put(bufferX + bufferY * width, min[x + y * PATTERN_SIZE])
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

/**
 * Naive implementation of difference between pattern and the given block.
 *
 * Could be improved by a smarter distance measure.
 */
private fun Array<Int>.differenceToBlock(
    buffer: IntBuffer,
    blockX: Int,
    blockY: Int,
    width: Int,
    height: Int,
): Int {
    var error = 0;

    for (y in 0 until PATTERN_SIZE) {
        for (x in 0 until PATTERN_SIZE) {
            val bufferX = (blockX * PATTERN_SIZE + x).coerceIn(0, width - 1)
            val bufferY = (blockY * PATTERN_SIZE + y).coerceIn(0, height - 1)

            val bufferIndex = bufferX + bufferY * width

            val bufferPixel = if (buffer[bufferIndex] < 128) 0 else 255
            error += abs(get(x + y * PATTERN_SIZE) - bufferPixel)
        }
    }

    return error
}

/**
 * The patterns to compare our blocks with.
 */
private val PATTERNS = mutableListOf(
    arrayOf(
        // empty
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
    ),
    arrayOf(
        // 12.5%
        0x00, 0x00, 0x00, 0x00,
        0xFF, 0x00, 0x00, 0x00,
        0x00, 0x00, 0xFF, 0x00,
        0x00, 0x00, 0x00, 0x00,
    ),
    arrayOf(
        // 25%
        0x00, 0x00, 0x00, 0x00,
        0xFF, 0x00, 0xFF, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0xFF, 0x00, 0xFF, 0x00,
    ),
    arrayOf(
        // 50%
        0x00, 0xFF, 0x00, 0xFF,
        0xFF, 0x00, 0xFF, 0x00,
        0x00, 0xFF, 0x00, 0xFF,
        0xFF, 0x00, 0xFF, 0x00,
    ),
    arrayOf(
        // 75%
        0xFF, 0xFF, 0x00, 0xFF,
        0xFF, 0x00, 0xFF, 0xFF,
        0x00, 0xFF, 0xFF, 0xFF,
        0xFF, 0x00, 0xFF, 0x00,
    ),
    arrayOf(
        // 100%
        0xFF, 0xFF, 0xFF, 0xFF,
        0xFF, 0xFF, 0xFF, 0xFF,
        0xFF, 0xFF, 0xFF, 0xFF,
        0xFF, 0xFF, 0xFF, 0xFF,
    ),
).addInverted()

fun MutableList<Array<Int>>.addInverted(): List<Array<Int>> {
    val outputList = mutableListOf<Array<Int>>()

    forEach { input ->
        val inverted = Array(input.size) { index -> 255 - input[index] }
        val rotated = Array(input.size) { index ->
            val x = index % PATTERN_SIZE
            val y = index / PATTERN_SIZE

            input[y + x * PATTERN_SIZE]
        }
        val rotatedInverted = Array(input.size) { index ->
            val x = index % PATTERN_SIZE
            val y = index / PATTERN_SIZE

            255 - input[y + x * PATTERN_SIZE]
        }

        outputList.add(input)
        outputList.add(inverted)
        outputList.add(rotated)
        outputList.add(rotatedInverted)
    }

    return outputList.toList()
}

private const val PATTERN_SIZE = 4