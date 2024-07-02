package de.berlindroid.zekompanion.server.ai

import de.berlindroid.zekompanion.base64
import de.berlindroid.zekompanion.ditherFloydSteinberg
import de.berlindroid.zekompanion.server.ext.ImageExt.toPixels
import de.berlindroid.zekompanion.toBinary
import de.berlindroid.zekompanion.zipit
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import javax.imageio.ImageIO

const val USER_PROFILE_PICTURE_SIZE = 32

private fun rescaleImage(
    image: BufferedImage,
    targetWidth: Int = USER_PROFILE_PICTURE_SIZE,
    targetHeight: Int = USER_PROFILE_PICTURE_SIZE,
): BufferedImage {
    val scaledImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)
    val destinationImage = BufferedImage(targetWidth, targetHeight, image.type)
    val graphics = destinationImage.createGraphics()
    graphics.drawImage(scaledImage, 0, 0, null)
    graphics.dispose()
    return destinationImage
}

class AI(
    private val gemini: Gemini = Gemini(),
    private val dale: Dalle = Dalle(),
    private val firstNames: MutableList<String> = mutableListOf(),
    private val lastNames: MutableList<String> = mutableListOf(),
    private val prefixes: List<String> = listOf(
        "Al",
        "Bint",
        "Ibn",
        "Mac",
        "Mc",
        "Nic",
        "Ní",
        "da ",
        "de ",
        "di ",
        "le ",
        "van ",
        "von ",
    ),
) {
    init {
        val names = this.javaClass.classLoader.getResource("names.txt")?.readText()
        names?.lines()?.forEachIndexed { index, name ->
            try {
                val (first, last) = name.split(" ")
                if (first !in firstNames) {
                    firstNames += first
                }

                if (last !in lastNames) {
                    lastNames += last
                }
            } catch (e: IndexOutOfBoundsException) {
                println("Couldn't split name '${name}' at index ${index}. Ignoring it.")
            }
        }
    }

    suspend fun createUserName(): String = "${firstNames.random()} ${createMaybeRandomPrefix()}${lastNames.random()}"

    private fun createMaybeRandomPrefix(): String = when (Math.random()) {
        in 0.95..1.0 -> prefixes.random()
        else -> ""
    }

    suspend fun createUserDescription(name: String): String = gemini.getDescription(name)
    suspend fun createUserChatPhrase(name: String, description: String): String = gemini.getChatPhrase(name, description)

    suspend fun createUserProfileImages(uuid: String, name: String, description: String): String? {
        val image = dale.requestImageGeneration(
            name = name,
            description = description,
        )

        return if (image != null) {
            ImageIO.write(image, "png", File("./profiles/${uuid}.png"))

            rescaleImage(image)
                .toPixels()
                .ditherFloydSteinberg(USER_PROFILE_PICTURE_SIZE, USER_PROFILE_PICTURE_SIZE)
                .toBinary()
                .zipit()
                .base64()
        } else {
            null
        }
    }
}
