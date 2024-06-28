package de.berlindroid.zekompanion.server.routers

import de.berlindroid.zekompanion.server.ai.AI
import de.berlindroid.zekompanion.server.user.User
import de.berlindroid.zekompanion.server.user.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import java.util.*


fun Route.adminCreateUser(users: UserRepository, ai: AI) =
    post("/api/user") {
        runCatching {
            ifAuthorized {
                val uuid = UUID.randomUUID().toString()
                val name = ai.createUserName()
                val description = ai.createUserDescription(name)
                val iconb64 = ai.createUserImage(name, description)

                val user = User(
                    uuid = uuid,
                    name = name,
                    description = description,
                    iconb64 = iconb64,
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
