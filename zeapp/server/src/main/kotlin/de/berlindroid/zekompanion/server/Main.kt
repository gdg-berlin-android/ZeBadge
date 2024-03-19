@file:JvmName("Main")

package de.berlindroid.zekompanion.server

import de.berlindroid.zekompanion.server.routers.imageBin
import de.berlindroid.zekompanion.server.routers.imagePng
import de.berlindroid.zekompanion.server.routers.index
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing

private const val DEFAULT_PORT = 8000

fun main(args: Array<String>) {
    val port = if (args.isNotEmpty()) {
        args.first().toInt()
    } else {
        DEFAULT_PORT
    }
    println("🪪Serving on port $port.")

    embeddedServer(Netty, port = port) {
        install(ContentNegotiation) {
            json()
        }
        routing {
            staticResources("/", "static") {
                index()
            }

            imageBin()
            imagePng()
        }
    }.start(wait = true)
}
