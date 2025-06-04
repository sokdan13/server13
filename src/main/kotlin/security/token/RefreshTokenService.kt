package com.example.security.token

import java.security.SecureRandom
import java.util.Base64

class RefreshTokenService: TokenService {
    override fun generate(
        config: BaseTokenConfig,
        vararg claims: TokenClaim
    ): String {
        val random = SecureRandom()
        val bytes = ByteArray((config as RefreshTokenConfig).length)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}