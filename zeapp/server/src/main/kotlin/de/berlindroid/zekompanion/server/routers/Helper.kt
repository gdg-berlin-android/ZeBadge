package de.berlindroid.zekompanion.server.routers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.response.respondText
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
