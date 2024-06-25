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


fun Route.postPost(zepass: ZePassRepository, users: UserRepository) =
    post("/api/zepass") {
        runCatching {
            val post = call.receive<Post>()

            val user = users.getUser(post.posterUUID)
            if (user != null) {
                val postUUID = zepass.newPost(post)

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
