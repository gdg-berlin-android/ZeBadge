package de.berlindroid.zekompanion.server.routers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingContext
import io.ktor.util.pipeline.PipelineContext
import de.berlindroid.zekompanion.server.user.User

private const val AUTH_TOKEN_ENV = "ZESERVER_AUTH_TOKEN"
private const val AUTH_TOKEN_HEADER = "ZeAuth"

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

suspend fun PipelineContext<Unit, ApplicationCall>.checkAuthorization(
    unauthorized: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit = {
        call.respondText(status = HttpStatusCode.Forbidden, text = "Forbidden")
    },
    authorized: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit,
) {
    val authHeader = call.request.header(AUTH_TOKEN_HEADER)
    val authEnv = System.getenv(AUTH_TOKEN_ENV)

    if (authEnv.isNullOrBlank()) {
        println(
            "Auth env ('${AUTH_TOKEN_ENV}') environment var is 'null' or empty, you will not be able to do admin level tasks. " +
                    "Set the env var and restart the server.",
        )
    }

    if (authEnv.isNullOrBlank() || authHeader == null || authEnv != authHeader) {
        unauthorized()
    } else {
        authorized()
    }
}

// RoutingContext extensions for Ktor 3.0 compatibility
// In Ktor 3.0, post{} blocks changed from PipelineContext to RoutingContext receiver type
suspend fun RoutingContext.checkAuthorization(
    unauthorized: suspend RoutingContext.() -> Unit = {
        call.respondText(status = HttpStatusCode.Forbidden, text = "Forbidden")
    },
    authorized: suspend RoutingContext.() -> Unit,
) {
    val authHeader = call.request.header(AUTH_TOKEN_HEADER)
    val authEnv = System.getenv(AUTH_TOKEN_ENV)

    if (authEnv.isNullOrBlank()) {
        println(
            "Auth env ('${AUTH_TOKEN_ENV}') environment var is 'null' or empty, you will not be able to do admin level tasks. " +
                    "Set the env var and restart the server.",
        )
    }

    if (authEnv.isNullOrBlank() || authHeader == null || authEnv != authHeader) {
        unauthorized()
    } else {
        authorized()
    }
}

suspend fun RoutingContext.withParameter(
    key: String,
    block: suspend RoutingContext.(value: String) -> Unit,
) {
    if (call.parameters.contains(key)) {
        val value = call.parameters[key]
        if (value != null) {
            block(value)
        }
    }
}

// User helper functions for RoutingContext
suspend fun RoutingContext.respondUser(user: User?) {
    if (user != null) {
        call.respond(status = HttpStatusCode.OK, user)
    } else {
        call.respondText(status = HttpStatusCode.NotFound, text = "Not Found.")
    }
}

suspend fun RoutingContext.processUser(user: User?) {
    if (user != null) {
        call.respondText(status = HttpStatusCode.OK, text = user.profileB64 ?: "")
    } else {
        call.respondText(status = HttpStatusCode.NotFound, text = "Not Found.")
    }
}
