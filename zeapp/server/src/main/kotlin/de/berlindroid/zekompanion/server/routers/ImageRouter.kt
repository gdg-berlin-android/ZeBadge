package de.berlindroid.zekompanion.server.routers

import de.berlindroid.zekompanion.BadgePayload.*
import de.berlindroid.zekompanion.base64
import de.berlindroid.zekompanion.server.ext.ImageExt.toImage
import de.berlindroid.zekompanion.server.ext.ImageExt.transform
import de.berlindroid.zekompanion.server.models.ImageRequest
import de.berlindroid.zekompanion.toBinary
import de.berlindroid.zekompanion.zipit
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.http.content.CompressedFileType
import io.ktor.server.http.content.StaticContentConfig
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.util.encodeBase64
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

fun StaticContentConfig<URL>.index() {
    default("static/index.html")
    preCompressed(CompressedFileType.GZIP)
}

fun Route.imageBin() =
    post("/api/image/bin") {
        runCatching {
            val image = call.receiveNullable<ImageRequest>() ?: throw IllegalArgumentException("Payload is null")

            val payload = PreviewPayload(
                payload = image.transform()
                    .toBinary()
                    .zipit()
                    .base64(),
            )

            call.respondText(payload.toBadgeCommand())
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }

fun Route.imagePng() =
    post("/api/image/png") {
        runCatching {
            val payload = call.receiveNullable<ImageRequest>() ?: throw IllegalArgumentException("Payload is null")

            val image = payload.transform().toImage(payload.width, payload.height)

            val stream = ByteArrayOutputStream()
            ImageIO.write(image, "png", stream)

            call.respondText(stream.toByteArray().encodeBase64())
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}", status = HttpStatusCode.MethodNotAllowed)
        }
    }
