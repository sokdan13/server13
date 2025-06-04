package com.example.security.token

interface TokenService {
    fun generate(
        config: BaseTokenConfig,
        vararg claims: TokenClaim
    ) : String
}