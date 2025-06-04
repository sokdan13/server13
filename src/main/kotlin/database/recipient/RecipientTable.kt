package com.example.database.recipient

import com.example.database.user.UserTable
import org.jetbrains.exposed.sql.Table

object RecipientTable : Table("recipients") {
    val userId = integer("user_id").references(UserTable.id)
    val recipientId = integer("recipient_id").autoIncrement()

    override val primaryKey = PrimaryKey(userId, recipientId)

    val name = varchar("name", 50)
    val address = varchar("address", 50)
    val phone = varchar("phone", 20)
}