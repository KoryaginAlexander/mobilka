package ru.mobilka.pr64.server.domain

import ru.mobilka.pr64.server.domain.model.NobelPrize

private val prizePathRegex = Regex(".*/nobelPrize/([a-z]+)/(\\d{4})$", RegexOption.IGNORE_CASE)

fun NobelPrize.categorySlug(): String {
    val href = links.firstOrNull { it.rel == "nobelPrize" }?.href
        ?: links.firstOrNull()?.href
        ?: error("Nobel prize ${awardYear} has no link to derive category")
    val match = prizePathRegex.find(href.trimEnd('/'))
        ?: error("Cannot parse category from href: $href")
    return match.groupValues[1].lowercase()
}
