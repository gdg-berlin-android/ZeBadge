package de.berlindroid.zeapp.zebits

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Base64
import java.util.zip.Deflater

/**
 * Helper to convert a bytearray to base64
 */
fun ByteArray.base64(): String = Base64.getEncoder().encodeToString(this)

/**
 * Take a base64 encoded string and convert it back to å bytearray.
 */
fun String.debase64(): ByteArray = Base64.getDecoder().decode(this)

/**
 * Compress a given byte array to a smaller byte array.
 */
fun ByteArray.zipit(): ByteArray {
    val deflater = Deflater(Deflater.BEST_COMPRESSION)
    deflater.reset()
    deflater.setInput(this)
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
    return result
}
