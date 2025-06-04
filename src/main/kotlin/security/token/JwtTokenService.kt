package com.example.security.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

class JwtTokenService: TokenService {
    override fun generate(
        config: BaseTokenConfig,
        vararg claims: TokenClaim
    ): String {
        var token = JWT.create()
            .withAudience((config as JWTTokenConfig).audience)
            .withIssuer(config.issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expiresIn))
        claims.forEach { claim ->
            token = when (val v = claim.value){
                is String -> token.withClaim(claim.name, v)
                is Int -> token.withClaim(claim.name, v)
                is Boolean -> token.withClaim(claim.name, v)
                is Long -> token.withClaim(claim.name, v)
                else -> token.withClaim(claim.name, v.toString())
            }

        }
        return token.sign(Algorithm.HMAC256(config.secret))
    }

}