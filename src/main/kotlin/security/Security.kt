package com.example.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.security.token.JWTTokenConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.respondText

fun Application.configureSecurity(jwtTokenConfig: JWTTokenConfig) {
    install(Authentication){
        jwt("jwt-auth"){
            realm = this@configureSecurity.environment.config.property("ktor.jwt.realm").getString()
            val jwtVerifier = JWT
                .require(Algorithm.HMAC256(jwtTokenConfig.secret))
                .withAudience(jwtTokenConfig.audience)
                .withIssuer(jwtTokenConfig.issuer)
                .build()

            verifier(jwtVerifier)

            validate { jwtCredential ->
                val username = jwtCredential.payload.getClaim("username").asString()
                if (!username.isNullOrBlank()){
                    JWTPrincipal(jwtCredential.payload)
                }else{
                    null
                }
            }

            challenge { _, _ ->
                call.respondText("Token is not valid or has expired",
                    status = HttpStatusCode.Unauthorized)
            }
        }
    }
}
