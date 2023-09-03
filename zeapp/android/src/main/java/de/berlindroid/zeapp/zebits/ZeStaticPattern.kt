package de.berlindroid.zeapp.zebits

import de.berlindroid.zekompanion.grayscale
import de.berlindroid.zekompanion.green
import de.berlindroid.zekompanion.map
import de.berlindroid.zekompanion.rgb
import java.nio.IntBuffer
import kotlin.math.abs

/**
 * Dither using a static pattern
 *
 * This dithering results in higher pixel error rate. It compares any PATTERN_SIZExPATTERN_SIZE pixel values with a given
 * set of PATTERN_SIZExPATTERN_SIZE patterns, and selects the one with the least pixel error.
 */
fun IntBuffer.ditherStaticPattern(width: Int, height: Int): IntBuffer {
    val output = grayscale()
    output.rewind()

    output.map { it.green() }

    // loop through pixels, one block at a time
    for (blockY in 0 until height / PATTERN_SIZE) {
        for (blockX in 0 until width / PATTERN_SIZE) {
            // find block with minimal error
            val min = PATTERNS.minBy { pattern ->
                pattern.differenceToBlock(output, blockX, blockY, width, height)
            }

            // put minimal error block in image
            for (y in 0 until PATTERN_SIZE) {
                for (x in 0 until PATTERN_SIZE) {
                    val bufferX = (blockX * PATTERN_SIZE + x).coerceIn(0, width - 1)
                    val bufferY = (blockY * PATTERN_SIZE + y).coerceIn(0, height - 1)

                    output.put(bufferX + bufferY * width, min[x + y * PATTERN_SIZE])
                }
            }
        }
    }

    // convert pixel values back to full color, coercing will be done by Color.rgb
    output.rewind()
    output.map {
        val value = if (it < 128) 0 else 255
        rgb(value, value, value)
    }

    return output
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
    var error = 0

    for (y in 0 until PATTERN_SIZE) {
        for (x in 0 until PATTERN_SIZE) {
            val bufferX = (blockX * PATTERN_SIZE + x).coerceIn(0, width - 1)
            val bufferY = (blockY * PATTERN_SIZE + y).coerceIn(0, height - 1)
            val bufferPixel = buffer[bufferX + bufferY * width]

            error += abs(get(x + y * PATTERN_SIZE) - bufferPixel)
        }
    }

    return error
}

private const val PATTERN_SIZE = 3

/**
 * The patterns to compare our blocks with.
 */
// private val PATTERNS = bruteForce() // ignored due to size constraints
private val PATTERNS = handPicked()

private fun handPicked() = mutableListOf(
    arrayOf(
        0x00, 0x00, 0x00,
        0x00, 0x00, 0x00,
        0x00, 0x00, 0x00,
    ),
    arrayOf(
        0x00, 0x00, 0x00,
        0x00, 0xFF, 0x00,
        0x00, 0x00, 0x00,
    ),
    arrayOf(
        0x00, 0xFF, 0x00,
        0x00, 0xFF, 0x00,
        0x00, 0x00, 0x00,
    ),
    arrayOf(
        0x00, 0x00, 0x00,
        0x00, 0xFF, 0x00,
        0x00, 0xFF, 0x00,
    ),
    arrayOf(
        0x00, 0xFF, 0x00,
        0x00, 0xFF, 0x00,
        0x00, 0xFF, 0x00,
    ),
    arrayOf(
        0x00, 0xFF, 0x00,
        0xFF, 0xFF, 0x00,
        0x00, 0xFF, 0x00,
    ),
    arrayOf(
        0x00, 0xFF, 0x00,
        0xFF, 0x00, 0x00,
        0x00, 0xFF, 0x00,
    ),
    arrayOf(
        0xFF, 0x00, 0xFF,
        0x00, 0xFF, 0x00,
        0xFF, 0x00, 0xFF,
    ),
    arrayOf(
        0x00, 0xFF, 0x00,
        0xFF, 0x00, 0xFF,
        0x00, 0xFF, 0x00,
    ),
).deriveMorePatterns()

fun MutableList<Array<Int>>.deriveMorePatterns(): List<Array<Int>> {
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
