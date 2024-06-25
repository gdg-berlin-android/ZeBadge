package de.berlindroid.zekompanion.server.routers

import de.berlindroid.zekompanion.server.user.User
import de.berlindroid.zekompanion.server.user.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.util.pipeline.PipelineContext


suspend fun PipelineContext<Unit, ApplicationCall>.withParameter(
    key: String,
    block: suspend PipelineContext<Unit, ApplicationCall>.(value: String) -> Unit,
) {
    if (call.parameters.contains(key)) {
        val value = call.parameters[key]
        if (value != null) {
            block(value)
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.ifAuthorized(block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit) {
    val authHeader = call.request.header("ZeAuth")
    val authEnv = System.getenv("ZESERVER_AUTH_TOKEN")
    if (authEnv.isEmpty() || authHeader == null || authEnv != authHeader) {
        call.respondText(status = HttpStatusCode.Forbidden, text = "Forbidden")
    } else {
        block()
    }
}

fun Route.adminCreateUser(users: UserRepository) =
    post("/api/user/") {
        runCatching {
            ifAuthorized {
                val newUser = call.receiveNullable<User>() ?: throw IllegalArgumentException("No user payload found.")
                val uuidAdded = users.createUser(newUser)

                if (uuidAdded != null) {
                    call.respond(status = HttpStatusCode.Created, users.getUser(uuidAdded)!!)
                } else {
                    call.respondText("invalid", status = HttpStatusCode.Forbidden)
                }

            }
        }.onFailure {
            it.printStackTrace()
            call.respondText("Error: ${it.message}")
        }
    }

fun Route.adminListUsers(users: UserRepository) =
    get("/api/user/") {
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
