package com.example.database.token

import kotlinx.datetime.Instant


data class Token(
    val userId: Int,
    val refreshToken: String,
    val issuedAt : Instant,
    val expiresAt: Instant,
    val revoked: Boolean
)
