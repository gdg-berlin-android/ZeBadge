package de.berlindroid.zekompanion.server.routers

import de.berlindroid.zekompanion.server.user.UserRepository
import de.berlindroid.zekompanion.server.zepass.Post
import de.berlindroid.zekompanion.server.zepass.ZePassRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*


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
            call.respond(zepass.getPosts())
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }
