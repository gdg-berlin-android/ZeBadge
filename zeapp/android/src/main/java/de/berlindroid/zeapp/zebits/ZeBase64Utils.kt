package de.berlindroid.zeapp.zebits

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.Base64
import java.util.zip.Deflater
import java.util.zip.Inflater

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
        var got = 0
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
        var got = 0
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
