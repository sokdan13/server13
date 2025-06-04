package com.example.security.routing

import com.example.database.token.Token
import com.example.database.token.TokenDataSource
import com.example.security.request.AuthRequest
import com.example.database.user.*
import com.example.security.hashing.HashingService
import com.example.security.request.RefreshRequest
import com.example.security.response.AuthResponse
import com.example.security.response.TokenResponse
import com.example.security.token.TokenClaim
import com.example.security.token.JWTTokenConfig
import com.example.security.token.JwtTokenService
import com.example.security.token.RefreshTokenConfig
import com.example.security.token.RefreshTokenService
import com.example.security.utils.ErrorCode
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.time.Duration.Companion.milliseconds

fun Application.authRouting(
    hashingService: HashingService,
    userDataSource: UserDataSource,
    jwtTokenService: JwtTokenService,
    jwtTokenConfig: JWTTokenConfig,
    refreshTokenConfig: RefreshTokenConfig,
    refreshTokenService: RefreshTokenService,
    tokenDataSource: TokenDataSource
) {
    routing {
        post("signup") {
            val requestData = kotlin.runCatching<AuthRequest?> { call.receiveNullable<AuthRequest>() }.getOrNull() ?: run {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthResponse<Unit>(
                        false,
                        ErrorCode.INCORRECT_CREDENTIALS
                    )
                )
                return@post
            }

            val areFieldsBlank = requestData.username.isBlank() || requestData.password.isBlank()
            val isPwdTooShort = requestData.password.length < 8
            val userExists = userDataSource.userExists(requestData.username)
            if (areFieldsBlank) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthResponse<Unit>(
                        false,
                        ErrorCode.BLANK_CREDENTIALS
                    )
                )
                return@post
            }
            if (isPwdTooShort) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthResponse<Unit>(
                        false,
                        ErrorCode.SHORT_PASSWORD
                    )
                )
                return@post
            }
            if (userExists) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthResponse<Unit>(
                        false,
                        ErrorCode.USER_ALREADY_EXISTS
                    )
                )
                return@post
            } else {
                val hash = hashingService.generateHash(requestData.password)
                val user = User(
                    username = requestData.username,
                    password = hash
                )

                val refreshToken = refreshTokenService.generate(refreshTokenConfig)

                val userId = newSuspendedTransaction {
                    val userId = userDataSource.insertUser(user)
                    userId?.let {
                        tokenDataSource.insertToken(
                            Token(
                                userId,
                                refreshToken,
                                Clock.System.now(),
                                Clock.System.now().plus(refreshTokenConfig.expiresIn.milliseconds),
                                false
                            )
                        )
                    }
                    userId
                }
                if (userId == null) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        AuthResponse<Unit>(
                            false,
                            ErrorCode.SERVER_ERROR
                        )
                    )
                    return@post
                }

                val accessToken = jwtTokenService.generate(
                    config = jwtTokenConfig,
                    TokenClaim(
                        name = "user_id",
                        value = userId
                    )
                )
                call.respond(
                    status = HttpStatusCode.OK,
                    message = AuthResponse(
                        success = true,
                        data = TokenResponse(accessToken, refreshToken)
                    )
                )
            }
        }
        post("/signin") {
            val requestData = kotlin.runCatching<AuthRequest?> { call.receiveNullable<AuthRequest>() }.getOrNull() ?: run {
                call.respond(
                    HttpStatusCode.BadRequest, AuthResponse<Unit>(
                        false,
                        ErrorCode.INCORRECT_CREDENTIALS
                    )
                )
                return@post
            }
            val foundUser = userDataSource.getUserByUsername(requestData.username)
            if (foundUser == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthResponse<Unit>(
                        false,
                        ErrorCode.USER_NOT_FOUND
                    )
                )
                return@post

            }

            val isValidPassword = hashingService.verify(
                value = requestData.password,
                hash = foundUser.user.password
            )

            if (!isValidPassword) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthResponse<Unit>(
                        false,
                        ErrorCode.INCORRECT_CREDENTIALS
                    )
                )
                return@post
            }

            val accessToken = jwtTokenService.generate(
                config = jwtTokenConfig,
                TokenClaim(
                    name = "user_id",
                    value = foundUser.userId
                )
            )

            val refreshToken = refreshTokenService.generate(refreshTokenConfig)

            val updatedRows = newSuspendedTransaction {
                tokenDataSource.updateToken(
                    Token(
                        foundUser.userId,
                        refreshToken,
                        Clock.System.now(),
                        Clock.System.now().plus(refreshTokenConfig.expiresIn.milliseconds),
                        false
                    )
                )
            }

            if (updatedRows < 1) {
                call.respond(
                    HttpStatusCode.Conflict,
                    AuthResponse<Unit>(
                        false,
                        ErrorCode.SERVER_ERROR
                    )
                )
                return@post
            }

            call.respond(
                status = HttpStatusCode.OK,
                message = AuthResponse(
                    success = true,
                    data = TokenResponse(accessToken, refreshToken)
                )
            )
        }

        post("refresh") {
            val requestData = kotlin.runCatching<RefreshRequest?> { call.receiveNullable<RefreshRequest>() }.getOrNull() ?: run {
                call.respond(
                    HttpStatusCode.BadRequest, AuthResponse<Unit>(
                        false,
                        ErrorCode.INCORRECT_CREDENTIALS
                    )
                )
                return@post
            }

            if (requestData.refreshToken.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest, AuthResponse<Unit>(
                        false,
                        ErrorCode.BLANK_CREDENTIALS
                    )
                )
                return@post
            }

            val foundToken = tokenDataSource.findToken(requestData.refreshToken)

            if (foundToken == null || foundToken.expiresAt > Clock.System.now()) {
                call.respond(
                    HttpStatusCode.Unauthorized, AuthResponse<Unit>(
                        false,
                        ErrorCode.SESSION_EXPIRED
                    )
                )
                return@post
            } else {
                val newAccessToken = jwtTokenService.generate(
                    config = jwtTokenConfig,
                    TokenClaim(
                        name = "user_id",
                        value = foundToken.userId
                    )
                )

                val refreshToken = refreshTokenService.generate(refreshTokenConfig)

                val updatedRows = tokenDataSource.updateToken(
                    Token(
                        foundToken.userId,
                        refreshToken,
                        Clock.System.now(),
                        Clock.System.now().plus(refreshTokenConfig.expiresIn.milliseconds),
                        false
                    )
                )
                if (updatedRows < 1) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        AuthResponse<Unit>(
                            false,
                            ErrorCode.SERVER_ERROR
                        )
                    )
                    return@post
                }
                call.respond(
                    status = HttpStatusCode.OK,
                    message = AuthResponse(
                        success = true,
                        data = TokenResponse(newAccessToken, foundToken.refreshToken)
                    )
                )
            }


        }

        authenticate("jwt-auth") {
            get("secret") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.getClaim("username", String::class)
                call.respond(HttpStatusCode.OK, "Your username is $username")
            }
        }
    }
}