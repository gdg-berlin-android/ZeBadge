package de.berlindroid.zeapp.hardware

import assertk.assertThat
import assertk.assertions.isEqualTo
import java.util.zip.Inflater
import org.junit.Test

class ZipitTest {

    @Test
    fun zipString() {
        val input = "Hello, world!"
        val expected = byteArrayOf(120, -38, -13, 72, -51, -55, -55, -41, 81, 40, -49, 47, -54, 73, 81, 4, 0, 32, 94, 4, -118)
        val actual = input.toByteArray().zipit()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun zipUnzip() {
        val input = "Hello, world!"
        val zipped = input.toByteArray().zipit()
        val unzipped = String(zipped.unzipByteArray())

        assertThat(unzipped).isEqualTo(input)
    }

    private fun ByteArray.unzipByteArray(): ByteArray {
        val inflater = Inflater()
        inflater.setInput(this)

        val buffer = ByteArray(1024)
        val output = mutableListOf<Byte>()

        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            output.addAll(buffer.take(count))
        }

        inflater.end()

        return output.toByteArray()
    }
}
