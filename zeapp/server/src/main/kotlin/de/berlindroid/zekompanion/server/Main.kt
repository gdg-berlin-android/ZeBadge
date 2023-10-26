@file:JvmName("Main")

package de.berlindroid.zekompanion.server

import de.berlindroid.zekompanion.BadgePayload
import de.berlindroid.zekompanion.base64
import de.berlindroid.zekompanion.debase64
import de.berlindroid.zekompanion.ditherFloydSteinberg
import de.berlindroid.zekompanion.grayscale
import de.berlindroid.zekompanion.invert
import de.berlindroid.zekompanion.resize
import de.berlindroid.zekompanion.server.Operation.FloydSteinberg
import de.berlindroid.zekompanion.server.Operation.Grayscale
import de.berlindroid.zekompanion.server.Operation.Invert
import de.berlindroid.zekompanion.server.Operation.Resize
import de.berlindroid.zekompanion.server.Operation.Threshold
import de.berlindroid.zekompanion.threshold
import de.berlindroid.zekompanion.toBinary
import de.berlindroid.zekompanion.zipit
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.CompressedFileType
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.util.encodeBase64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.IntBuffer
import javax.imageio.ImageIO

@Serializable
sealed class Operation {
    @Serializable
    @SerialName("FloydSteinberg")
    data class FloydSteinberg(val width: Int, val height: Int) : Operation()

    @Serializable
    @SerialName("Resize")
    data class Resize(val width: Int, val height: Int) : Operation()

    @Serializable
    @SerialName("Threshold")
    data class Threshold(val threshold: Int) : Operation()

    @Serializable
    @SerialName("Invert")
    data object Invert : Operation()

    @Serializable
    @SerialName("Grayscale")
    data object Grayscale : Operation()
}

@Serializable
data class ImageRequest(
    val operations: List<Operation> = emptyList(),
    val image: String = "",
    val width: Int = -1,
    val height: Int = -1,
)

fun main(args:Array<String>) {
    val port = if (args.isNotEmpty()){
        args.first().toInt()
    } else {
        8000
    }
    println("🪪Serving on port $port.")

    embeddedServer(Netty, port = port) {
        install(ContentNegotiation) {
            json()
        }

        routing {
            staticResources("/", "static") {
                default("static/index.html")
                preCompressed(CompressedFileType.GZIP)
            }

            post("/api/image/bin") {
                try {
                    val image = call.receiveNullable<ImageRequest>() ?: throw IllegalArgumentException("Payload is null")

                    val payload = BadgePayload(
                        debug = false,
                        type = "preview",
                        meta = "",
                        payload = image.transform()
                            .toBinary()
                            .zipit()
                            .base64(),
                    )

                    call.respondText(payload.toBadgeCommand())
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondText("Error: ${e.message}")
                }
            }

            post("/api/image/png") {
                try {
                    val payload = call.receiveNullable<ImageRequest>() ?: throw IllegalArgumentException("Payload is null")

                    val image = payload.transform().toImage(payload.width, payload.height)

                    val stream = ByteArrayOutputStream()
                    ImageIO.write(image, "png", stream)

                    call.respondText(stream.toByteArray().encodeBase64())
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondText("Error: ${e.message}", status = HttpStatusCode.MethodNotAllowed)
                }

            }
        }
    }.start(wait = true)
}

private fun IntBuffer.toImage(width: Int, height: Int): BufferedImage {
    val output = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    output.setRGB(0, 0, width, height, array(), 0, width)
    return output
}

private fun ImageRequest.transform(): IntBuffer {
    var image = image.debase64().toImage().toPixels()

    operations.forEach { operation ->
        image = when (operation) {
            is FloydSteinberg -> image.ditherFloydSteinberg(operation.width, operation.height)
            is Resize -> image.resize(width, height, operation.width, operation.height)
            is Threshold -> image.threshold(operation.threshold)
            is Invert -> image.invert()
            is Grayscale -> image.grayscale()
        }
    }
    return image
}

private fun BufferedImage.toPixels(): IntBuffer {
    val output = IntBuffer.allocate(width * height)
    getRGB(0, 0, width, height, output.array(), 0, width)
    return output
}

private fun ByteBuffer.toImage(): BufferedImage {
    val stream = ByteArrayInputStream(array())
    val image = ImageIO.read(stream)
    return image
}
