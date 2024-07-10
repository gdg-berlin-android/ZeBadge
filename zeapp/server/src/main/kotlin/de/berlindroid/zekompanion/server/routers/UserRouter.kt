package de.berlindroid.zekompanion.server.routers

import de.berlindroid.zekompanion.server.ai.AI
import de.berlindroid.zekompanion.server.user.User
import de.berlindroid.zekompanion.server.user.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondOutputStream
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.util.pipeline.PipelineContext
import java.awt.image.BufferedImage
import java.io.File
import java.util.UUID
import javax.imageio.ImageIO


fun Route.adminCreateUser(users: UserRepository, ai: AI) =
    post("/api/user") {
        runCatching {
            checkAuthorization {
                val uuid = UUID.randomUUID().toString()
                val name = ai.createUserName()
                val description = ai.createUserDescription(name)
                val chatPhrase = ai.createUserChatPhrase(name, description)

                val b64 = ai.createUserProfileImages(uuid, name, description)

                val user = User(
                    uuid = uuid,
                    name = name,
                    description = description,
                    profileB64 = b64,
                    chatPhrase = chatPhrase,
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

fun Route.adminCreateUserBadge(users: UserRepository) =
    get("/api/user/{uuid}/badge") {
        runCatching {
            withParameter("uuid") { uuid ->
                checkAuthorization {
                    val user = users.getUser(uuid)
                    if (user != null) {
                        val baseBadgeResource = javaClass.classLoader.getResource("zeAlternativeBadge.bmp")
                        val baseBadge = ImageIO.read(baseBadgeResource)
                        val resultBadge = BufferedImage(baseBadge.width, baseBadge.height, BufferedImage.TYPE_INT_RGB)

                        val profileImageFile = File("./profiles/${uuid}.png")
                        val profile = ImageIO.read(profileImageFile)

                        val g = resultBadge.graphics
                        g.drawImage(baseBadge, 0, 0, 296, 128, null)
                        g.drawImage(profile, 16, 16, 128 - 32, 128 - 32, null)

                        g.font = g.font.deriveFont(34f)
                        user.name.split(' ').forEachIndexed { index, part ->
                            g.drawString(part, 124, 54 + index * 40)
                        }

                        call.respondOutputStream {
                            ImageIO.write(resultBadge, "bmp", this)
                        }
                    }
                }
            }
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}.")
        }
    }


fun Route.adminDeleteUser(users: UserRepository) =
    delete("/api/user/{UUID}") {
        runCatching {
            checkAuthorization {
                withParameter("UUID") { uuid ->
                    call.respond(status = HttpStatusCode.OK, users.deleteUser(uuid))
                }
            }
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }

fun Route.listUsers(users: UserRepository) =
    get("/api/user") {
        runCatching {
            checkAuthorization(
                authorized = {
                    call.respond(status = HttpStatusCode.OK, users.getUsers())
                },
                unauthorized = {
                    call.respond(status = HttpStatusCode.OK, users.getIndexedUsers())
                },
            )
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
                checkAuthorization(
                    unauthorized = {
                        val index = newUser.uuid.toIntOrNull()
                        if (index != null) {
                            val userUpdated = users.updateUserByIndex(index, newUser)

                            if (userUpdated) {
                                call.respondText(text = "OK")
                            } else {
                                call.respondText("invalid user", status = HttpStatusCode.NotAcceptable)
                            }
                        } else {
                            call.respondText("invalid index", status = HttpStatusCode.NotAcceptable)
                        }
                    },
                    authorized = {
                        val userUpdated = users.updateUser(newUser.copy(uuid = uuid))

                        if (userUpdated) {
                            call.respondText(text = "OK")
                        } else {
                            call.respondText("invalid", status = HttpStatusCode.NotAcceptable)
                        }
                    },
                )
            }
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}", status = HttpStatusCode.NotAcceptable)
        }
    }

fun Route.getUser(users: UserRepository) =
    get("/api/user/{UUID}") {
        runCatching {
            suspend fun PipelineContext<Unit, ApplicationCall>.respondUser(user: User?) {
                if (user != null) {
                    call.respond(status = HttpStatusCode.OK, user)
                } else {
                    call.respondText(status = HttpStatusCode.NotFound, text = "Not Found.")
                }
            }

            withParameter("UUID") { uuid ->
                checkAuthorization(
                    unauthorized = {
                        val user = users.getUserByIndex(uuid.toIntOrNull() ?: -1)
                        respondUser(user)
                    },
                    authorized = {
                        val user = users.getUser(uuid)
                        respondUser(user)
                    },
                )
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
            suspend fun PipelineContext<Unit, ApplicationCall>.processUser(user: User?) {
                if (user != null) {
                    call.respondFile(
                        File("./profiles/${user.uuid}.png"),
                    )
                } else {
                    call.respondText(status = HttpStatusCode.NotFound, text = "Not Found.")
                }
            }

            withParameter("UUID") { uuid ->
                checkAuthorization(
                    unauthorized = {
                        processUser(
                            users.getUserByIndex(uuid.toIntOrNull() ?: -1),
                        )
                    },
                    authorized = {
                        processUser(users.getUser(uuid))
                    },
                )
                call.respondText(status = HttpStatusCode.UnprocessableEntity, text = "No UUID.")
            }
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }

fun Route.getUserProfileImageBinary(users: UserRepository) =
    get("/api/user/{UUID}/b64") {
        runCatching {
            withParameter("UUID") { uuid ->

                suspend fun PipelineContext<Unit, ApplicationCall>.processUser(user: User?) {
                    if (user != null) {
                        call.respondText(status = HttpStatusCode.OK, text = user.profileB64 ?: "")
                    } else {
                        call.respondText(status = HttpStatusCode.NotFound, text = "Not Found.")
                    }
                }

                checkAuthorization(
                    unauthorized = {
                        processUser(
                            users.getUserByIndex(uuid.toIntOrNull() ?: -1),
                        )
                    },
                    authorized = {
                        processUser(users.getUser(uuid))
                    },
                )
                call.respondText(status = HttpStatusCode.UnprocessableEntity, text = "No UUID.")
            }
            call.respondText(status = HttpStatusCode.UnprocessableEntity, text = "No UUID.")
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }
