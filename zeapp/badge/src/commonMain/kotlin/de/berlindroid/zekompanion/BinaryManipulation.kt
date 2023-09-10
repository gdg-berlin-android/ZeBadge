package de.berlindroid.zekompanion

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.Base64
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.experimental.or
import kotlin.math.floor

/**
 * Helper to convert a bytearray to base64
 */
fun ByteBuffer.base64(): String = Base64.getEncoder().encodeToString(array())

/**
 * Take a base64 encoded string and convert it back to a bytearray.
 */
fun String.debase64(): ByteBuffer {
    val bytes = Base64.getDecoder().decode(this)
    val result = ByteBuffer.allocate(bytes.size)
    result.put(bytes)
    return result
}

/**
 * Compress a given byte buffer to a smaller byte buffer.
 */
fun ByteBuffer.zipit(): ByteBuffer {
    val deflater = Deflater(Deflater.BEST_COMPRESSION)
    deflater.reset()
    deflater.setInput(array())
    deflater.finish()

    var result = ByteArray(0)
    val o = ByteArrayOutputStream(1)
    try {
        val buf = ByteArray(64)
        var got: Int
        while (!deflater.finished()) {
            got = deflater.deflate(buf)
            o.write(buf, 0, got)
        }
        result = o.toByteArray()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    } finally {
        try {
            o.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        deflater.end()
    }

    return ByteBuffer.wrap(result)
}

/**
 * Decompress a given byte zipped buffer to a bigger byte buffer.
 */
fun ByteBuffer.unzipit(): ByteBuffer {
    val deflater = Inflater()
    deflater.reset()
    deflater.setInput(array())

    var result = ByteArray(0)
    val o = ByteArrayOutputStream(1)
    try {
        val buf = ByteArray(64)
        var got: Int
        while (!deflater.finished()) {
            got = deflater.inflate(buf)
            o.write(buf, 0, got)
        }
        result = o.toByteArray()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    } finally {
        try {
            o.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        deflater.end()
    }

    val output = ByteBuffer.allocate(result.size)
    output.put(result)
    return output
}

/**
 * Linear threshold all values above limit to white, and below to black
 *
 * @param limit the value to be considered the threshold, defaults to 128, half of the range
 */
fun IntBuffer.threshold(limit: Int = 128): IntBuffer {
    val output = copy()

    output.map { input ->
        val gray = input.gray().green()

        if (gray > limit) {
            0xffffff
        } else {
            0x000000
        }
    }

    output.rewind()
    return output
}

/**
 * Create new bitmap containing only the luminance values of all pixels.
 */
fun IntBuffer.grayscale(): IntBuffer {
    val output = copy()

    output.map { input ->
        input.gray()
    }

    return output
}

/**
 * Create gray from a color.
 */
fun Int.gray(): Int {
    val (r, g, b) = rgb()
    val gray = (.2126 * r + 0.7152 * g + 0.0722 * b).toInt()
    return rgb(gray, gray, gray)
}

/**
 * tupelize a color
 */
fun Int.rgb(): List<Int> = listOf(red(), green(), blue())

/**
 * Linear invert all pixel values in the buffer
 *
 * This will work best with images in black/white or grayscale.
 */
fun IntBuffer.invert(): IntBuffer {
    val output = copy()

    output.map { input ->
        val (r, g, b) = input.rgb()
        rgb(255 - r, 255 - g, 255 - b)
    }

    output.rewind()
    return output
}

fun Int.red() = (this shr 16) and 0xff
fun Int.green() = (this shr 8) and 0xff
fun Int.blue() = (this shr 0) and 0xff

/**
 * tupelize to color
 */
fun rgb(r: Int, g: Int, b: Int): Int =
    ((r and 0xff) shl 16) or
            ((g and 0xff) shl 8) or
            ((b and 0xff) shl 0)

/**
 * Converts a given black/white buffer of pixels to a byte buffer of pixels per bit.
 */
fun IntBuffer.toBinary(): ByteBuffer {

    val output = mutableListOf<Byte>()

    var bitIndex = 0
    var currentByte: Byte = 0
    forEach { pixel ->
        val value = pixel.green()
        currentByte = currentByte or ((if (value == 255) 1 else 0) shl (7 - bitIndex)).toByte()
        bitIndex += 1

        if (bitIndex == Byte.SIZE_BITS) {
            output.add(currentByte)
            bitIndex = 0
            currentByte = 0
        }
    }

    return output.toBuffer()
}

private fun List<Byte>.toBuffer(): ByteBuffer = ByteBuffer.wrap(toTypedArray().toByteArray())

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
fun ByteBuffer.forEach(mapper: (it: Byte) -> Unit) {
    for (i in 0 until limit()) {
        mapper(get(i))
    }
}

/**
 * Iterate over all values of an IntBuffer
 */
fun IntBuffer.forEachIndexed(
    exitIf: (() -> Boolean)? = null,
    mapper: (index: Int, it: Int) -> Unit,
) {
    if (exitIf != null) {
        for (i in 0 until limit()) {
            mapper(i, get(i))
            if (exitIf()) {
                break
            }
        }
    } else {
        for (i in 0 until limit()) {
            mapper(i, get(i))
        }
    }
}

/**
 * Simple scaling of images, no filtering
 */
fun IntBuffer.resize(inputWidth: Int, inputHeight: Int, outputWidth: Int, outputHeight: Int): IntBuffer {
    val output = IntBuffer.allocate(outputWidth * outputHeight)

    for (y in 0 until outputHeight) {
        val realtiveY = y / outputHeight.toFloat()
        val inputY = floor(realtiveY * inputHeight).toInt()

        for (x in 0 until outputWidth) {
            val realtiveX = x / outputWidth.toFloat()
            val inputX = floor(realtiveX * inputWidth).toInt()

            output.put(get(inputX + inputY * inputWidth))
        }
    }

    return output
}

fun Int.isBinary(): Boolean {
    val (r, g, b) = rgb()

    return r == g && g == b && (r == 0 || r == 255)
}

fun IntBuffer.copy(): IntBuffer = IntBuffer.wrap(array())
