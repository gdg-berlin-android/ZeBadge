package de.berlindroid.zekompanion.server.ext

import de.berlindroid.zekompanion.debase64
import de.berlindroid.zekompanion.ditherFloydSteinberg
import de.berlindroid.zekompanion.grayscale
import de.berlindroid.zekompanion.invert
import de.berlindroid.zekompanion.resize
import de.berlindroid.zekompanion.server.models.ImageRequest
import de.berlindroid.zekompanion.server.models.Operation
import de.berlindroid.zekompanion.threshold
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.IntBuffer
import javax.imageio.ImageIO

object ImageExt {
    fun BufferedImage.toPixels(): IntBuffer {
        val output = IntBuffer.allocate(width * height)
        getRGB(0, 0, width, height, output.array(), 0, width)
        return output
    }

    fun ByteBuffer.toImage(): BufferedImage {
        val stream = ByteArrayInputStream(array())
        val image = ImageIO.read(stream)
        return image
    }

    fun IntBuffer.toImage(width: Int, height: Int): BufferedImage {
        val output = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        output.setRGB(0, 0, width, height, array(), 0, width)
        return output
    }

    fun ImageRequest.transform(): IntBuffer {
        var image = image.debase64().toImage().toPixels()

        operations.forEach { operation ->
            image = when (operation) {
                is Operation.FloydSteinberg -> image.ditherFloydSteinberg(operation.width, operation.height)
                is Operation.Resize -> image.resize(width, height, operation.width, operation.height)
                is Operation.Threshold -> image.threshold(operation.threshold)
                is Operation.Invert -> image.invert()
                is Operation.Grayscale -> image.grayscale()
            }
        }
        return image
    }
}
