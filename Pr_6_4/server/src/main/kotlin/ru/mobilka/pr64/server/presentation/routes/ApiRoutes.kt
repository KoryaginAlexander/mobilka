package ru.mobilka.pr64.server.presentation.routes

import io.github.smiley4.ktoropenapi.delete as docDelete
import io.github.smiley4.ktoropenapi.get as docGet
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.post as docPost
import io.github.smiley4.ktorredoc.redoc
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import ru.mobilka.pr64.server.application.FavoritesUseCase
import ru.mobilka.pr64.server.application.ListPrizesUseCase
import ru.mobilka.pr64.server.application.LoginUseCase
import ru.mobilka.pr64.server.application.RegisterUseCase
import ru.mobilka.pr64.server.presentation.dto.ErrorResponse
import ru.mobilka.pr64.server.presentation.dto.LoginRequest
import ru.mobilka.pr64.server.presentation.dto.LoginResponse
import ru.mobilka.pr64.server.presentation.dto.PrizeItemDto
import ru.mobilka.pr64.server.presentation.dto.PrizesListResponse
import ru.mobilka.pr64.server.presentation.dto.RegisterRequest
import ru.mobilka.pr64.server.presentation.util.userId

@Suppress("LongMethod")
fun Application.configureHttpRoutes(
    loginUseCase: LoginUseCase,
    registerUseCase: RegisterUseCase,
    listPrizesUseCase: ListPrizesUseCase,
    favoritesUseCase: FavoritesUseCase,
) {
    routing {
        route("/api.json") {
            openApi()
        }
        route("/swagger") {
            swaggerUI("/api.json") {
                persistAuthorization = true
            }
        }
        route("/redoc") {
            redoc("/api.json")
        }

        docPost("/login", {
            protected = false
            securitySchemeNames(emptyList())
            description = "JWT-авторизация по логину и паролю"
            request {
                body<LoginRequest>()
            }
            response {
                code(HttpStatusCode.OK) {
                    description = "Токен доступа"
                    body<LoginResponse>()
                }
                code(HttpStatusCode.Unauthorized) {
                    description = "Неверные учётные данные"
                    body<ErrorResponse>()
                }
            }
        }) {
            val body = call.receive<LoginRequest>()
            val token = loginUseCase.execute(body.username, body.password)
            if (token == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("invalid_credentials", "Неверный логин или пароль"),
                )
                return@docPost
            }
            call.respond(LoginResponse(token = token))
        }

        docPost("/register", {
            protected = false
            securitySchemeNames(emptyList())
            description = "Регистрация пользователя с ролью USER"
            request {
                body<RegisterRequest>()
            }
            response {
                code(HttpStatusCode.Created) {
                    description = "Пользователь создан"
                }
                code(HttpStatusCode.Conflict) {
                    body<ErrorResponse>()
                }
                code(HttpStatusCode.BadRequest) {
                    body<ErrorResponse>()
                }
            }
        }) {
            val body = call.receive<RegisterRequest>()
            when (registerUseCase.execute(body.username, body.password)) {
                RegisterUseCase.RegisterResult.Created ->
                    call.respond(HttpStatusCode.Created, mapOf("status" to "created"))

                RegisterUseCase.RegisterResult.UsernameTaken ->
                    call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse("username_taken", "Имя пользователя занято"),
                    )

                RegisterUseCase.RegisterResult.ValidationError ->
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            "validation_error",
                            "Логин ≥ 3 символов, пароль ≥ 8 символов",
                        ),
                    )
            }
        }

        docGet("/prizes", {
            protected = false
            securitySchemeNames(emptyList())
            description = "Список премий из PostgreSQL; при первом запросе/просрочке кэша — загрузка из Nobel API"
            request {
                queryParameter<Boolean>("refresh") {
                    description = "Принудительно обновить данные из api.nobelprize.org"
                    required = false
                }
            }
            response {
                code(HttpStatusCode.OK) {
                    body<PrizesListResponse>()
                }
            }
        }) {
            val refresh = call.request.queryParameters["refresh"]?.toBooleanStrictOrNull() ?: false
            val payload = listPrizesUseCase(refresh)
            call.respond(payload)
        }

        authenticate("auth-jwt") {
            docGet("/favorites", {
                protected = true
                description = "Избранные премии текущего пользователя"
                response {
                    code(HttpStatusCode.OK) {
                        body<List<PrizeItemDto>>()
                    }
                }
            }) {
                val principal = call.principal<JWTPrincipal>() ?: error("Unauthorized")
                val uid = principal.userId()
                call.respond(favoritesUseCase.list(uid))
            }

            docPost("/favorites/{prizeId}", {
                protected = true
                description = "Добавить премию в избранное"
                request {
                    pathParameter<Int>("prizeId") {
                        description = "Идентификатор премии в локальной БД"
                    }
                }
                response {
                    code(HttpStatusCode.NoContent) {
                        description = "Добавлено или уже было в избранном"
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Премия не найдена"
                        body<ErrorResponse>()
                    }
                }
            }) {
                val prizeId = call.parameters["prizeId"]?.toIntOrNull()
                    ?: return@docPost call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("invalid_id", "prizeId должен быть числом"),
                    )
                val principal = call.principal<JWTPrincipal>() ?: error("Unauthorized")
                val uid = principal.userId()
                when (favoritesUseCase.add(uid, prizeId)) {
                    FavoritesUseCase.FavoriteMutationResult.Ok ->
                        call.respond(HttpStatusCode.NoContent)

                    FavoritesUseCase.FavoriteMutationResult.AlreadyAdded ->
                        call.respond(HttpStatusCode.NoContent)

                    FavoritesUseCase.FavoriteMutationResult.PrizeNotFound ->
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("not_found", "Премия не найдена"),
                        )

                    FavoritesUseCase.FavoriteMutationResult.NotFavorite ->
                        call.respond(HttpStatusCode.NoContent)
                }
            }

            docDelete("/favorites/{prizeId}", {
                protected = true
                description = "Удалить премию из избранного"
                request {
                    pathParameter<Int>("prizeId") {}
                }
                response {
                    code(HttpStatusCode.NoContent) {}
                    code(HttpStatusCode.NotFound) { body<ErrorResponse>() }
                }
            }) {
                val prizeId = call.parameters["prizeId"]?.toIntOrNull()
                    ?: return@docDelete call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("invalid_id", "prizeId должен быть числом"),
                    )
                val principal = call.principal<JWTPrincipal>() ?: error("Unauthorized")
                val uid = principal.userId()
                when (favoritesUseCase.remove(uid, prizeId)) {
                    FavoritesUseCase.FavoriteMutationResult.Ok ->
                        call.respond(HttpStatusCode.NoContent)

                    FavoritesUseCase.FavoriteMutationResult.NotFavorite ->
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("not_favorite", "Нет в избранном"),
                        )

                    else ->
                        call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}
