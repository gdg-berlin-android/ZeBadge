@file:JvmName("Main")

package de.berlindroid.zekompanion.server

import de.berlindroid.zekompanion.server.ai.AI
import de.berlindroid.zekompanion.server.routers.adminCreateUser
import de.berlindroid.zekompanion.server.routers.adminCreateUserBadge
import de.berlindroid.zekompanion.server.routers.adminDeleteUser
import de.berlindroid.zekompanion.server.routers.getOptimizedPosts
import de.berlindroid.zekompanion.server.routers.getPosts
import de.berlindroid.zekompanion.server.routers.getResizedUserProfileImagePng
import de.berlindroid.zekompanion.server.routers.getUser
import de.berlindroid.zekompanion.server.routers.getUserProfileImageBinary
import de.berlindroid.zekompanion.server.routers.getUserProfileImagePng
import de.berlindroid.zekompanion.server.routers.imageBin
import de.berlindroid.zekompanion.server.routers.imagePng
import de.berlindroid.zekompanion.server.routers.listUsers
import de.berlindroid.zekompanion.server.routers.postPost
import de.berlindroid.zekompanion.server.routers.updateUser
import de.berlindroid.zekompanion.server.user.UserRepository
import de.berlindroid.zekompanion.server.zepass.ZePassRepository
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.tomcat.Tomcat
import java.security.KeyStore

private const val SSL_PASSWORD_ENV = "SSL_CERTIFICATE_PASSWORD"
private const val KEYSTORE_RESOURCE_FILE = "/tmp/keystore.jks"

fun main() {
    val users = UserRepository.load()
    val zepass = ZePassRepository.load()

    embeddedBadgeServer(users, zepass)
        .start(wait = false)

    embeddedWebServer(users, zepass)
        .start(wait = true)
}

private fun embeddedBadgeServer(
    users: UserRepository,
    zepass: ZePassRepository,
): EmbeddedServer<*, *> {
    return embeddedServer(
        Tomcat,
        port = 1337,
    ) {
        install(ContentNegotiation) {
            json()
        }

        routing {
            get("/") {
                call.respondText("yes")
            }

            postPost(zepass, users)
            getOptimizedPosts(zepass, users)
        }
    }
}

private fun embeddedWebServer(
    users: UserRepository,
    zepass: ZePassRepository,
): EmbeddedServer<*, *> {
    val keyPassword = try {
        System.getenv(SSL_PASSWORD_ENV)
    } catch (e: Exception) {
        null
    }

    val keyStore: KeyStore? = loadKeyStore(keyPassword)
    val ai = AI()

    return embeddedServer(
        Tomcat,
        port = 8080,
    ) {
        install(ContentNegotiation) {
            json()
        }

        routing {
            staticResources("/", "static")

            imageBin()
            imagePng()

            adminCreateUser(users, ai)
            adminDeleteUser(users)
            adminCreateUserBadge(users)

            listUsers(users)
            updateUser(users)
            getUser(users)
            getUserProfileImageBinary(users)
            getUserProfileImagePng(users)
            getResizedUserProfileImagePng(users)

            postPost(zepass, users)
            getPosts(zepass)
        }
    }
}

private fun loadKeyStore(keyPassword: String?): KeyStore? {
    var keyStore: KeyStore? = null

    if (keyPassword == null) {
        println("... NO PASSWORD SET ON '$SSL_PASSWORD_ENV' ENVIRONMENT VAR -> NO SSL ENCRYPTION ...")
    } else {
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        val stream = jksInResourcesStream()
        val keyArray = keyPassword.toCharArray()
        keyStore.load(stream, keyArray)

        println("Certificate and password found.")
        println("• Found ${keyStore.aliases().toList().count()} aliases.")
        keyStore.aliases()?.asIterator()?.forEach { println("• '$it'.") }
    }

    return keyStore
}

private fun jksInResourcesStream() = object {}.javaClass.getResourceAsStream(KEYSTORE_RESOURCE_FILE)
