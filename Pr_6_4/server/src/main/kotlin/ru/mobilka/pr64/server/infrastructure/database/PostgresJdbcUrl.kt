package ru.mobilka.pr64.server.infrastructure.database

import ru.mobilka.pr64.server.infrastructure.config.ServerEnv
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Neon и другие провайдеры отдают URI вида postgres://user:pass@host/db?sslmode=require.
 * HikariCP ожидает JDBC URL.
 */
fun resolveJdbcUrlFromEnv(): String {
    val explicit = ServerEnv.get("JDBC_DATABASE_URL")
    if (explicit != null) return explicit

    val uriString = ServerEnv.get("DATABASE_URL")
        ?: error(
            "Set DATABASE_URL or JDBC_DATABASE_URL (env), or put them in server/.env — see server/.env.example",
        )

    if (uriString.startsWith("jdbc:")) return uriString

    val uri = URI(uriString)
    require(uri.scheme == "postgres" || uri.scheme == "postgresql") {
        "DATABASE_URL должен быть postgres:// или postgresql://"
    }

    val userInfo = uri.userInfo ?: ""
    val colon = userInfo.indexOf(':')
    val user = if (colon >= 0) userInfo.substring(0, colon) else userInfo
    val password = if (colon >= 0) userInfo.substring(colon + 1) else ""

    val host = uri.host ?: error("host в DATABASE_URL отсутствует")
    val port = if (uri.port > 0) uri.port else 5432
    val path = uri.path.trim('/').ifEmpty { error("имя БД в пути отсутствует") }

    val query = uri.rawQuery?.let { "?$it" } ?: ""
    val needsSsl = !uriString.contains("sslmode=", ignoreCase = true)
    val suffix = buildString {
        if (query.isEmpty()) append("?") else append("&")
        append("user=").append(URLEncoder.encode(user, StandardCharsets.UTF_8))
        append("&password=").append(URLEncoder.encode(password, StandardCharsets.UTF_8))
        if (needsSsl) append("&sslmode=require")
        // Neon serverless: короткоживущие соединения; без server-side prepared statements стабильнее
        if (!uriString.contains("prepareThreshold=", ignoreCase = true)) {
            append("&prepareThreshold=0")
        }
        if (!uriString.contains("tcpKeepAlive=", ignoreCase = true)) {
            append("&tcpKeepAlive=true")
        }
    }

    return "jdbc:postgresql://$host:$port/$path$query$suffix"
}
