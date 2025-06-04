package com.example.security.token

data class RefreshTokenConfig(
    override val expiresIn: Long,
    val length : Int
) : BaseTokenConfig