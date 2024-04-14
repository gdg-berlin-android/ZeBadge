package de.berlindroid.zekompanion

import assertk.Assert
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.fail
import org.junit.Test
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import java.io.FileNotFoundException
import java.nio.IntBuffer
import javax.imageio.ImageIO

private const val BADGE_W = 296
private const val BADGE_H = 128

class BitmapTest {
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
    fun onlyBinaryPixelAfterThresholding() {
        val input = IntBuffer.wrap(pixels)
        val output = input.threshold()

        assertThat(output.array()).containsOnly(0, 0xFFFFFF)
    }

    @Test
    fun onlyBinaryPixelAfterFloydSteinberg() {
        val subject = IntBuffer.wrap(pixels).ditherFloydSteinberg(width, height)

        assertThat(subject.array()).containsOnly(0, 0xFFFFFF)
    }

    @Test
    fun floydSteinbergStillWorksAsExpected() {
        val subject = IntBuffer.wrap(pixels)
            .resize(width, height, BADGE_W, BADGE_H)
            .ditherFloydSteinberg(BADGE_W, BADGE_H)
            .toBufferedImage(BADGE_W, BADGE_H)
        val fs = ImageIO.read(javaClass.classLoader?.getResource("./voltron-fs.png"))

        assertThat(subject).allPixelEqualTo(fs)
    }

    @Test
    fun thresholdingStillWorksAsExpected() {
        val subject = IntBuffer.wrap(pixels)
            .resize(width, height, BADGE_W, BADGE_H)
            .threshold()
            .toBufferedImage(BADGE_W, BADGE_H)
        val threshold = ImageIO.read(javaClass.classLoader?.getResource("./voltron-threshold.png"))

        assertThat(subject).allPixelEqualTo(threshold)
    }

    @Test
    fun hardcodedResultAfterFS() {
        val input = IntBuffer.wrap(pixels)
        val output = input
            .resize(width, height, 10, 10)
            .ditherFloydSteinberg(10, 10)
            .toBinary()
            .zipit()
            .base64()

        assertThat(output).isEqualTo("eNpjYHBYIMHHcHCBV/19ABJOBBA=")
    }
}

fun Assert<BufferedImage>.allPixelEqualTo(other: BufferedImage) = given { actual ->
    val inputPixel = other.pixels
    actual.pixels.forEachIndexed { index, pixel ->
        if (pixel != inputPixel[index]) {
            fail("pixel mismatch on index $index.", other, actual)
        }
    }
}

val BufferedImage.pixels: List<Int>
    get() {
        val result = IntArray(width * height)
        this.getRGB(0, 0, width, height, result, 0, width)
        return result.toList()
    }

fun IntBuffer.toBufferedImage(width: Int, height: Int): BufferedImage {
    val image = BufferedImage(width, height, TYPE_INT_RGB)
    image.setRGB(0, 0, width, height, array(), 0, width)
    return image
}

fun IntBuffer.save(filename: String, width: Int, height: Int) =
    ImageIO.write(toBufferedImage(width, height), "png", File(filename))
