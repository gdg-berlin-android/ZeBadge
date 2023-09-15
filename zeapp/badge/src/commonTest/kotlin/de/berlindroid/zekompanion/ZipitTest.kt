package de.berlindroid.zekompanion

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test
import java.nio.ByteBuffer

class ZipitTest {

    @Test
    fun zipString() {
        val input = ByteBuffer.wrap("Hello, world!".toByteArray())

        val expected =
            ByteBuffer.wrap(byteArrayOf(120, -38, -13, 72, -51, -55, -55, -41, 81, 40, -49, 47, -54, 73, 81, 4, 0, 32, 94, 4, -118))

        val actual = input.zipit()
        assertThat(actual.array()).isEqualTo(expected.array())
    }

    @Test
    fun zipUnzip() {
        val input = "Hello, world!"
        val zipped = ByteBuffer.wrap(input.toByteArray()).zipit()
        val unzipped = String(zipped.unzipit().array())

        assertThat(unzipped).isEqualTo(input)
    }
}
