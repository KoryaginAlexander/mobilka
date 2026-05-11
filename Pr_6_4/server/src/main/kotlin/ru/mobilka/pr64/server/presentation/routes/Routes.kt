package ru.mobilka.pr64.server.presentation.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import ru.mobilka.pr64.server.application.GetAllPrizesUseCase
import ru.mobilka.pr64.server.application.GetLaureatesUseCase
import ru.mobilka.pr64.server.application.GetPrizeDetailUseCase
import ru.mobilka.pr64.server.application.LoginUseCase
import ru.mobilka.pr64.server.domain.model.LaureatesEnvelope
import ru.mobilka.pr64.server.presentation.dto.ErrorResponse
import ru.mobilka.pr64.server.presentation.dto.LoginRequest
import ru.mobilka.pr64.server.presentation.dto.LoginResponse

private val YearPattern = Regex("^\\d{4}$")

fun Application.configureRoutes(
    loginUseCase: LoginUseCase,
    getAllPrizes: GetAllPrizesUseCase,
    getPrizeDetail: GetPrizeDetailUseCase,
    getLaureates: GetLaureatesUseCase,
) {
    routing {
        post("/auth/login") {
            val body = call.receive<LoginRequest>()
            val token = loginUseCase.execute(body.username.trim(), body.password)
            if (token == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("invalid_credentials", "Неверный логин или пароль"),
                )
                return@post
            }
            call.respond(
                LoginResponse(accessToken = token),
            )
        }

        authenticate("auth-jwt") {
            prizeRoutes(getAllPrizes, getPrizeDetail, getLaureates)
        }
    }
}

private fun Route.prizeRoutes(
    getAllPrizes: GetAllPrizesUseCase,
    getPrizeDetail: GetPrizeDetailUseCase,
    getLaureates: GetLaureatesUseCase,
) {
    route("/prizes") {
        get {
            call.respond(getAllPrizes())
        }

        get("{year}/{category}/laureates") {
            val year = call.parameters["year"].orEmpty()
            val category = call.parameters["category"].orEmpty().lowercase()
            if (!year.matches(YearPattern)) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("invalid_year", "Год должен быть четырёхзначным числом"),
                )
                return@get
            }
            val prize = getPrizeDetail(year, category)
            if (prize == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("not_found", "Премия не найдена"),
                )
                return@get
            }
            call.respond(LaureatesEnvelope(getLaureates(year, category)))
        }

        get("{year}/{category}") {
            val year = call.parameters["year"].orEmpty()
            val category = call.parameters["category"].orEmpty().lowercase()
            if (!year.matches(YearPattern)) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("invalid_year", "Год должен быть четырёхзначным числом"),
                )
                return@get
            }
            val prize = getPrizeDetail(year, category)
            if (prize == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("not_found", "Премия не найдена"),
                )
                return@get
            }
            call.respond(prize)
        }
    }
}
