package ru.mobilka.pr64.server.presentation.plugins

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.AuthScheme
import io.github.smiley4.ktoropenapi.config.AuthType
import io.github.smiley4.ktoropenapi.config.OutputFormat
import io.ktor.server.application.Application
import io.ktor.server.application.install

fun Application.configureOpenApiPlugin() {
    install(OpenApi) {
        outputFormat = OutputFormat.JSON
        info {
            title = "Nobel Prize Service"
            description =
                "Авторизация JWT, кэш премий в PostgreSQL с синхронизацией из публичного Nobel API, избранное."
            version = "2.0.0"
        }
        // Относительный URL — «Try it out» бьёт в тот же host, что и страница Swagger
        server {
            url = "/"
            description = "Тот же origin, что у Swagger/ReDoc"
        }
        server {
            url = "http://127.0.0.1:8080"
            description = "127.0.0.1"
        }
        server {
            url = "http://localhost:8080"
            description = "localhost"
        }
        security {
            securityScheme("bearerAuth") {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "JWT"
            }
            defaultSecuritySchemeNames("bearerAuth")
            defaultUnauthorizedResponse {
                description =
                    "Нужен JWT: сначала POST /login, затем заголовок Authorization: Bearer <token>"
            }
        }
    }
}
