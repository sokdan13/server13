package com.example.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*

fun Application.configureDatabases() {
    val postgres = environment.config.config("ktor.postgres")
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = postgres.property("url").getString()
        username = postgres.property("user").getString()
        driverClassName = "org.postgresql.Driver"
        password = postgres.property("password").getString()
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    }
    val dataSource = HikariDataSource(hikariConfig)

    Database.connect(dataSource)
}