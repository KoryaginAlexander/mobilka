package ru.mobilka.pr64.server.infrastructure.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object UsersTable : IntIdTable("users") {
    val username = varchar("username", 255).uniqueIndex()
    val passwordHash = text("password_hash")
    val role = varchar("role", 32).default("USER")
}

object PrizesTable : IntIdTable("prizes") {
    val awardYear = integer("award_year")
    val category = varchar("category", 16)
    val fullName = text("full_name")
    val motivation = text("motivation").nullable()
    val detailLink = text("detail_link").nullable()

    init {
        uniqueIndex("uq_prizes_year_category", awardYear, category)
    }
}

object LaureatesTable : IntIdTable("laureates") {
    val prizeId = reference("prize_id", PrizesTable, onDelete = ReferenceOption.CASCADE)
    val fullName = text("full_name")
    val portion = varchar("portion", 64).nullable()
    val motivation = text("motivation").nullable()
    val portraitUrl = text("portrait_url").nullable()
}

object UserPrizesTable : Table("user_prizes") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val prizeId = reference("prize_id", PrizesTable, onDelete = ReferenceOption.CASCADE)
    val addedAt = long("added_at")

    override val primaryKey = PrimaryKey(userId, prizeId)
}

object AppMetadataTable : Table("app_metadata") {
    val key = varchar("key", 128)
    val value = text("value")

    override val primaryKey = PrimaryKey(key)
}
