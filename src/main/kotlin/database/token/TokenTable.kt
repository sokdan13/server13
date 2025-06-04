package com.example.database.token


import com.example.database.user.UserTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object TokenTable: IntIdTable("tokens") {
    val userId = integer("user_id").references(UserTable.id)
    val refreshToken = varchar("refresh_token",100).uniqueIndex()
    val issuedAt = timestamp("issued_at")
    val expiresAt = timestamp("expires_at")
    val revoked = bool("revoked")
}