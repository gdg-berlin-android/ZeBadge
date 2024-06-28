@file:JvmName("Main")

package de.berlindroid.zekompanion.server

import de.berlindroid.zekompanion.server.ai.AI
import de.berlindroid.zekompanion.server.routers.imageBin
import de.berlindroid.zekompanion.server.routers.imagePng
import de.berlindroid.zekompanion.server.routers.index
import de.berlindroid.zekompanion.server.routers.adminCreateUser
import de.berlindroid.zekompanion.server.routers.adminDeleteUser
import de.berlindroid.zekompanion.server.routers.adminListUsers
import de.berlindroid.zekompanion.server.routers.getPosts
import de.berlindroid.zekompanion.server.routers.getUser
import de.berlindroid.zekompanion.server.routers.postPost
import de.berlindroid.zekompanion.server.routers.updateUser
import de.berlindroid.zekompanion.server.user.UserRepository
import de.berlindroid.zekompanion.server.zepass.ZePassRepository
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.tomcat.Tomcat
import java.io.File
import java.security.KeyStore

private const val LOCAL_HTTP_PORT = 8000
private const val LOCAL_TLS_PORT = 8443
private const val SSL_PASSWORD_ENV = "SSL_CERTIFICATE_PASSWORD"
private const val KEYSTORE_RESOURCE_FILE = "/tmp/keystore.jks"

fun main(args: Array<String>) {
    val keyPassword = try {
        System.getenv(SSL_PASSWORD_ENV)
    } catch (e: Exception) {
        null
    }

    val keyStore: KeyStore? = loadKeyStore(keyPassword)
    val serverPort = extractServerPort(args, keyStore)
    println("Serving on port $serverPort.")

    val users = UserRepository.load()
    val ai = AI()
    val zepass = ZePassRepository.load()

    embeddedServer(
        Tomcat,
        environment = applicationEngineEnvironment {
            injectTLSIfNeeded(keyStore, keyPassword)

            module {
                install(ContentNegotiation) {
                    json()
                }

                routing {
                    staticResources("/", "static") {
                        index()
                        exclude { file ->
                            file.path.endsWith("db")
                        }
                    }

                    imageBin()
                    imagePng()

                    // Callable from ZeFlasher only?
                    adminCreateUser(users, ai)
                    adminListUsers(users)
                    adminDeleteUser(users)

                    // TODO: Check if callable from ZeBadge (no ssl)
                    updateUser(users)
                    getUser(users)

                    postPost(zepass, users)
                    getPosts(zepass)
                }
            }
        },
    ).start(wait = true)
}

private fun ApplicationEngineEnvironmentBuilder.injectTLSIfNeeded(keyStore: KeyStore?, keyPassword: String?) {
    if (keyStore != null && keyPassword != null) {
        sslConnector(
            keyStore = keyStore,
            keyAlias = "zealias",
            keyStorePassword = { keyPassword.toCharArray() },
            privateKeyPassword = { keyPassword.toCharArray() },
        ) {
            keyStorePath = File(KEYSTORE_RESOURCE_FILE)
        }
    }
}

private fun extractServerPort(args: Array<String>, keyStore: KeyStore?): Int {
    val serverPort = if (args.isNotEmpty()) {
        args.first().toInt()
    } else {
        if (keyStore != null) {
            LOCAL_TLS_PORT
        } else {
            LOCAL_HTTP_PORT
        }
    }
    return serverPort
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
