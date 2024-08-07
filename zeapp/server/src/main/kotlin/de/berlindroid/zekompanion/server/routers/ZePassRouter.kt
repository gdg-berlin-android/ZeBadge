package de.berlindroid.zekompanion.server.routers

import de.berlindroid.zekompanion.server.user.UserRepository
import de.berlindroid.zekompanion.server.zepass.Post
import de.berlindroid.zekompanion.server.zepass.ZePassRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.util.UUID


fun Route.postPost(zepass: ZePassRepository, users: UserRepository) =
    post("/api/zepass") {
        runCatching {
            val uuid = call.receive<String>()
            val user = users.getUser(uuid)
            if (user != null) {
                val postUUID = UUID.randomUUID().toString()
                zepass.newPost(
                    Post(
                        uuid = postUUID,
                        posterUUID = uuid,
                        message = user.chatPhrase ?: "",
                    ),
                )

                call.respond(status = HttpStatusCode.OK, postUUID)
            } else {
                call.respondText(status = HttpStatusCode.Unauthorized, text = "Nope.")
            }
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }

fun Route.getPosts(zepass: ZePassRepository) =
    get("/api/zepass") {
        runCatching {
            checkAuthorization(
                authorized = {
                    call.respond(zepass.getPosts())
                },
                unauthorized = {
                    call.respond(
                        zepass.getPosts().mapIndexed() { index, post ->
                            post.copy(posterUUID = "$index")
                        },
                    )
                },
            )
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }

fun Route.getOptimizedPosts(zepass: ZePassRepository, users: UserRepository) =
    get("/api/zepass") {
        runCatching {
            call.respond(zepass.getOptimizedPosts(users))
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }
