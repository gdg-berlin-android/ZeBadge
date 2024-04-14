package de.berlindroid.zekompanion

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.junit.Ignore
import org.junit.Test
import java.io.FileNotFoundException
import java.nio.IntBuffer
import javax.imageio.ImageIO

class CarvingTest {
    private val voltron = javaClass.classLoader.getResource("voltron.png")
    private val inputImage = ImageIO.read(
        voltron ?: throw FileNotFoundException("base image voltron not found."),
    )

    private val width = inputImage.width
    private val height = inputImage.height

    private val pixels = IntArray(width * height)

    init {

        assertThat(width).isEqualTo(169)
        assertThat(height).isEqualTo(169)

        inputImage.getRGB(0, 0, width, height, pixels, 0, width)
    }

    @Test
    fun resizeAndCarve() {
        val input = IntBuffer.wrap(pixels)

        val output = input.resizeAndCarve(width, height, 100, 100)
        assertThat(output.limit()).isEqualTo(100 * 100)

        val outputBmp = output.toBufferedImage(100, 100)
        val expectedBmp = ImageIO.read(
            javaClass.classLoader.getResource("voltron-resized-carved-100x100.png")
                ?: throw FileNotFoundException("resized and carved voltron not found."),
        )

        assertThat(outputBmp).allPixelEqualTo(expectedBmp)
    }

    //@Test
    fun generateTestImages() {
        val inputBmp = ImageIO.read(
            javaClass.classLoader.getResource("droidcon-photo.png")
                ?: throw FileNotFoundException("field expected image not found."),
        )

        val input = IntBuffer.wrap(inputBmp.pixels.toIntArray())
        input
            .resize(
                inputBmp.width, inputBmp.height,
                296, 128,
            ).ditherFloydSteinberg(
                296, 128,
            ).save(
                "/tmp/droidcon-photo-resized.png",
                296, 128,
            )

        input
            .resizeAndCarve(
                inputBmp.width, inputBmp.height,
                296, 128,
            ).ditherFloydSteinberg(
                296, 128,
            ).save(
                "/tmp/droidcon-photo-resized-carved.png",
                296, 128,
            )
    }

    @Test
    fun resizeCarveAndFS() {
        val inputBmp = ImageIO.read(
            javaClass.classLoader.getResource("fields.png")
                ?: throw FileNotFoundException("field expected image not found."),
        )
        val input = IntBuffer.wrap(inputBmp.pixels.toIntArray())

        val output = input
            .resizeAndCarve(
                inputBmp.width, inputBmp.height,
                296, 128,
            ).ditherFloydSteinberg(
                296, 128,
            )

        assertThat(output.limit()).isEqualTo(296 * 128)

        val outputBmp = output.toBufferedImage(296, 128)
        val expectedBmp = ImageIO.read(
            javaClass.classLoader.getResource("fields-296x128-fs.png")
                ?: throw FileNotFoundException("field expected image not found."),
        )

        assertThat(outputBmp).allPixelEqualTo(expectedBmp)
    }

    @Test
    fun carveHorizontal() {
        val input = IntBuffer.wrap(
            arrayOf(
                128, 0, 256,
                512, 0, 768,
                1024, 0, 2048,
            ).toIntArray(),
        )

        val output = input.carve(3, 3, 2, 3)

        assertThat(output.limit()).isEqualTo(2 * 3)
        assertThat(output.array()).containsExactly(
            128, 256,
            512, 768,
            1024, 2048,
        )
    }

    @Test
    fun carveVertical() {
        val input = IntBuffer.wrap(
            arrayOf(
                2, 4, 8,
                16, 0, 32,
                0, 64, 0,
            ).toIntArray(),
        )

        val output = input.carve(3, 3, 3, 2)
        assertThat(output.limit()).isEqualTo(2 * 3)
        assertThat(output.array()).containsExactly(
            2, 4, 8,
            16, 64, 32,
        )
    }

    @Test
    fun carve() {
        val input = IntBuffer.wrap(
            arrayOf(
                16, 0, 32,
                0, 0, 0,
                256, 0, 1024,
            ).toIntArray(),
        )

        val output = input.carve(3, 3, 2, 2)
        assertThat(output.limit()).isEqualTo(2 * 2)
        assertThat(output.array()).containsExactly(
            16, 32,
            256, 1024,
        )
    }

    @Test
    fun carveImage() {
        val input = IntBuffer.wrap(pixels)

        val output = input.carve(width, height, 100, 100)
        assertThat(output.limit()).isEqualTo(100 * 100)

        val outputBmp = output.toBufferedImage(100, 100)
        val expectedBmp = ImageIO.read(
            javaClass.classLoader.getResource("voltron-carved-100x100.png")
                ?: throw FileNotFoundException("carved voltron not found."),
        )

        assertThat(outputBmp).allPixelEqualTo(expectedBmp)
    }

    @Test
    fun resizeAndCarveImage() {
        val input = IntBuffer.wrap(pixels)

        val output = input.resizeAndCarve(width, height, 100, 100)
        assertThat(output.limit()).isEqualTo(100 * 100)

        val outputBmp = output.toBufferedImage(100, 100)
        val expectedBmp = ImageIO.read(
            javaClass.classLoader.getResource("voltron-resized-carved-100x100.png")
                ?: throw FileNotFoundException("resized and carved voltron not found."),
        )

        assertThat(outputBmp).allPixelEqualTo(expectedBmp)
    }
}
