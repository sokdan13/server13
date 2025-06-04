package com.example

import com.example.database.token.TokenDataSource
import com.example.database.user.UserDataSource
import com.example.security.hashing.HashingService
import com.example.security.routing.authRouting
import com.example.security.token.JWTTokenConfig
import com.example.security.token.JwtTokenService
import com.example.security.token.RefreshTokenConfig
import com.example.security.token.RefreshTokenService
import io.ktor.server.application.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    jwtTokenService: JwtTokenService,
    jwtTokenConfig: JWTTokenConfig,
    refreshTokenConfig: RefreshTokenConfig,
    refreshTokenService: RefreshTokenService,
    tokenDataSource: TokenDataSource
) {
    authRouting(hashingService, userDataSource, jwtTokenService, jwtTokenConfig, refreshTokenConfig, refreshTokenService, tokenDataSource)
}
