package com.example.security.token

data class JWTTokenConfig(
    val issuer : String,
    val audience : String,
    override val expiresIn : Long,
    val secret : String
) : BaseTokenConfig
