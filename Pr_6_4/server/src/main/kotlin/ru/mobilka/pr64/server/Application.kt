package ru.mobilka.pr64.server

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import ru.mobilka.pr64.server.application.GetAllPrizesUseCase
import ru.mobilka.pr64.server.application.GetLaureatesUseCase
import ru.mobilka.pr64.server.application.GetPrizeDetailUseCase
import ru.mobilka.pr64.server.application.LoginUseCase
import ru.mobilka.pr64.server.data.InMemoryNobelPrizeRepository
import ru.mobilka.pr64.server.data.NobelJsonLoader
import ru.mobilka.pr64.server.infrastructure.auth.JwtService
import ru.mobilka.pr64.server.presentation.plugins.configureJwtAuthentication
import ru.mobilka.pr64.server.presentation.plugins.configureMonitoring
import ru.mobilka.pr64.server.presentation.plugins.configureSerialization
import ru.mobilka.pr64.server.presentation.plugins.configureStatusPages
import ru.mobilka.pr64.server.presentation.routes.configureRoutes

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val jwtSecret =
        System.getenv("JWT_SECRET")
            ?: "nobel-prize-mock-server-development-secret-key-min-32-chars!!"

    val jwtIssuer = System.getenv("JWT_ISSUER") ?: "nobel-prize-mock-server"
    val jwtAudience = System.getenv("JWT_AUDIENCE") ?: "nobel-api-clients"

    val demoUser = System.getenv("API_USER") ?: "admin"
    val demoPassword = System.getenv("API_PASSWORD") ?: "nobel-admin"

    val jwtService = JwtService(
        secret = jwtSecret,
        issuer = jwtIssuer,
        audience = jwtAudience,
        validitySeconds = 30 * 60,
    )

    val prizes = NobelJsonLoader.loadFromResource()
    val repository = InMemoryNobelPrizeRepository(prizes)

    val loginUseCase = LoginUseCase(
        jwtService = jwtService,
        users = mapOf(demoUser to demoPassword),
    )
    val getAllPrizes = GetAllPrizesUseCase(repository)
    val getPrizeDetail = GetPrizeDetailUseCase(repository)
    val getLaureates = GetLaureatesUseCase(repository)

    configureMonitoring()
    configureSerialization()
    configureJwtAuthentication(jwtService)
    configureStatusPages()
    configureRoutes(
        loginUseCase = loginUseCase,
        getAllPrizes = getAllPrizes,
        getPrizeDetail = getPrizeDetail,
        getLaureates = getLaureates,
    )
}
