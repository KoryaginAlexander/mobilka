package ru.mobilka.pr64.server.presentation.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

/**
 * Swagger UI / ReDoc в браузере делают запросы с того же origin;
 * если в OpenAPI указан другой host (localhost vs 127.0.0.1), без CORS браузер режет запросы.
 */
fun Application.configureCors() {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
    }
}
