package ru.mobilka.pr64.server.infrastructure.config

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Читает переменные: сначала [System.getenv], затем ключи из файлов `.env`
 * (удобно при `./gradlew :server:run` из IDE, где нет export в shell).
 */
object ServerEnv {
    private val fromFile: MutableMap<String, String> = mutableMapOf()

    fun loadDotEnvFiles() {
        fromFile.clear()
        val base = Path.of(System.getProperty("user.dir", "."))
        val candidates = listOf(
            base.resolve("server").resolve(".env"),
            base.resolve(".env"),
        )
        for (path in candidates) {
            if (!Files.isRegularFile(path)) continue
            parseDotEnv(Files.readString(path, StandardCharsets.UTF_8))
        }
    }

    private fun parseDotEnv(text: String) {
        for (rawLine in text.lineSequence()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) continue
            val eq = line.indexOf('=')
            if (eq <= 0) continue
            val key = line.substring(0, eq).trim()
            var value = line.substring(eq + 1).trim()
            if (value.length >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length - 1)
            }
            if (key.isNotEmpty()) {
                fromFile[key] = value
            }
        }
    }

    fun get(name: String): String? {
        val env = System.getenv(name)?.trim()?.takeIf { it.isNotEmpty() }
        if (env != null) return env
        return fromFile[name]?.trim()?.takeIf { it.isNotEmpty() }
    }
}
