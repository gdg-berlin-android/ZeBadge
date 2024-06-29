package de.berlindroid.zekompanion.server.routers

import de.berlindroid.zekompanion.debase64
import de.berlindroid.zekompanion.fromBinaryToRGB
import de.berlindroid.zekompanion.server.ai.AI
import de.berlindroid.zekompanion.server.ai.USER_PROFILE_PICTURE_SIZE
import de.berlindroid.zekompanion.server.ext.ImageExt.toImage
import de.berlindroid.zekompanion.server.user.User
import de.berlindroid.zekompanion.server.user.UserRepository
import de.berlindroid.zekompanion.unzipit
import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import java.util.*
import javax.imageio.ImageIO


fun Route.adminCreateUser(users: UserRepository, ai: AI) =
    post("/api/user") {
        runCatching {
            ifAuthorized {
                val uuid = UUID.randomUUID().toString()
                val name = ai.createUserName()
                val description = ai.createUserDescription(name)
                val iconB64 = ai.createUserImage(name, description)

                val user = User(
                    uuid = uuid,
                    name = name,
                    description = description,
                    iconB64 = iconB64,
                )

                val uuidAdded = users.createUser(user)

                if (uuidAdded != null) {
                    call.respond(status = HttpStatusCode.Created, users.getUser(uuidAdded)!!)
                } else {
                    call.respondText("invalid", status = HttpStatusCode.Forbidden)
                }

            }
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}.")
        }
    }

fun Route.adminListUsers(users: UserRepository) =
    get("/api/user") {
        runCatching {
            ifAuthorized {
                call.respond(status = HttpStatusCode.OK, users.getUsers())
            }
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }

fun Route.adminDeleteUser(users: UserRepository) =
    delete("/api/user/{UUID}") {
        runCatching {
            ifAuthorized {
                withParameter("UUID") { uuid ->
                    call.respond(status = HttpStatusCode.OK, users.deleteUser(uuid))
                }
            }
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }

fun Route.updateUser(users: UserRepository) =
    put("/api/user/{UUID}") {
        runCatching {
            withParameter("UUID") { uuid ->
                val newUser = call.receiveNullable<User>() ?: throw IllegalArgumentException("No user payload found.")
                val userUpdated = users.updateUser(newUser.copy(uuid = uuid))

                if (userUpdated) {
                    call.respondText(text = "OK")
                } else {
                    call.respondText("invalid", status = HttpStatusCode.NotAcceptable)
                }
            }
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}", status = HttpStatusCode.NotAcceptable)
        }
    }

fun Route.getUser(users: UserRepository) =
    get("/api/user/{UUID}") {
        runCatching {
            withParameter("UUID") { uuid ->
                val user = users.getUser(uuid)
                if (user != null) {
                    call.respond(status = HttpStatusCode.OK, user)
                } else {
                    call.respondText(status = HttpStatusCode.NotFound, text = "Not Found.")
                }
            }
            call.respondText(status = HttpStatusCode.UnprocessableEntity, text = "No UUID.")
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }

fun Route.getUserProfileImagePng(users: UserRepository) =
    get("/api/user/{UUID}/png") {
        runCatching {
            withParameter("UUID") { uuid ->
                val user = users.getUser(uuid)
                if (user != null) {
                    val pixels = user.iconB64.debase64().unzipit().fromBinaryToRGB()
                    val image = pixels.toImage(USER_PROFILE_PICTURE_SIZE, USER_PROFILE_PICTURE_SIZE)

                    call.respondOutputStream(contentType = ContentType.Image.PNG) {
                        ImageIO.write(image, "png", this)
                    }
                } else {
                    call.respondText(status = HttpStatusCode.NotFound, text = "Not Found.")
                }
            }
            call.respondText(status = HttpStatusCode.UnprocessableEntity, text = "No UUID.")
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }

fun Route.getUserProfileImageBinary(users: UserRepository) =
    get("/api/user/{UUID}/b64") {
        runCatching {
            withParameter("UUID") { uuid ->
                val user = users.getUser(uuid)
                if (user != null) {
                    call.respondText { user.iconB64 }
                } else {
                    call.respondText(status = HttpStatusCode.NotFound, text = "Not Found.")
                }
            }
            call.respondText(status = HttpStatusCode.UnprocessableEntity, text = "No UUID.")
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }
