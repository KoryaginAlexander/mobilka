package ru.mobilka.pr64.server.presentation.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import ru.mobilka.pr64.server.presentation.dto.ErrorResponse

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error(cause.message, cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = "internal_error",
                    message = cause.message,
                ),
            )
        }
    }
}
