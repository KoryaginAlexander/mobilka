package ru.mobilka.pr64.server

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.json.Json
import ru.mobilka.pr64.server.application.FavoritesUseCase
import ru.mobilka.pr64.server.application.ListPrizesUseCase
import ru.mobilka.pr64.server.application.LoginUseCase
import ru.mobilka.pr64.server.application.RegisterUseCase
import ru.mobilka.pr64.server.data.FavoriteRepositoryExposed
import ru.mobilka.pr64.server.data.NobelRemoteClient
import ru.mobilka.pr64.server.data.PrizeRepositoryExposed
import ru.mobilka.pr64.server.data.UserRepositoryExposed
import ru.mobilka.pr64.server.infrastructure.auth.JwtService
import ru.mobilka.pr64.server.infrastructure.config.ServerEnv
import ru.mobilka.pr64.server.infrastructure.database.DatabaseFactory
import ru.mobilka.pr64.server.presentation.plugins.configureCors
import ru.mobilka.pr64.server.presentation.plugins.configureJwtAuthentication
import ru.mobilka.pr64.server.presentation.plugins.configureMonitoring
import ru.mobilka.pr64.server.presentation.plugins.configureOpenApiPlugin
import ru.mobilka.pr64.server.presentation.plugins.configureSerialization
import ru.mobilka.pr64.server.presentation.plugins.configureStatusPages
import ru.mobilka.pr64.server.presentation.routes.configureHttpRoutes

fun main() {
    ServerEnv.loadDotEnvFiles()
    val port = ServerEnv.get("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val jwtSecret =
        ServerEnv.get("JWT_SECRET")
            ?: "nobel-prize-mock-server-development-secret-key-min-32-chars!!"

    val jwtIssuer = ServerEnv.get("JWT_ISSUER") ?: "nobel-prize-mock-server"
    val jwtAudience = ServerEnv.get("JWT_AUDIENCE") ?: "nobel-api-clients"

    val syncIntervalMs =
        ServerEnv.get("SYNC_INTERVAL_MS")?.toLongOrNull() ?: 86_400_000L

    DatabaseFactory.connectAndMigrate()

    val httpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = ServerEnv.get("NOBEL_HTTP_TIMEOUT_MS")?.toLongOrNull() ?: 180_000L
            connectTimeoutMillis = 60_000L
            socketTimeoutMillis = 180_000L
        }
        install(ClientContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
    }

    val userRepository = UserRepositoryExposed()
    val prizeRepository = PrizeRepositoryExposed()
    val favoriteRepository = FavoriteRepositoryExposed(prizeRepository)
    val nobelRemoteClient = NobelRemoteClient(httpClient)

    val jwtService = JwtService(
        secret = jwtSecret,
        issuer = jwtIssuer,
        audience = jwtAudience,
        validitySeconds = 30 * 60,
    )

    val loginUseCase = LoginUseCase(userRepository, jwtService)
    val registerUseCase = RegisterUseCase(userRepository)
    val listPrizesUseCase = ListPrizesUseCase(prizeRepository, nobelRemoteClient, syncIntervalMs)
    val favoritesUseCase = FavoritesUseCase(favoriteRepository, prizeRepository)

    configureOpenApiPlugin()
    configureCors()
    configureMonitoring()
    configureSerialization()
    configureJwtAuthentication(jwtService)
    configureStatusPages()
    configureHttpRoutes(
        loginUseCase = loginUseCase,
        registerUseCase = registerUseCase,
        listPrizesUseCase = listPrizesUseCase,
        favoritesUseCase = favoritesUseCase,
    )
}
