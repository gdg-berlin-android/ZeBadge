package de.berlindroid.zekompanion.server.routers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.response.respondText
import io.ktor.util.pipeline.PipelineContext

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
