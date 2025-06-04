package com.example.security.hashing

import at.favre.lib.crypto.bcrypt.BCrypt

class BcryptHashingService: HashingService {
    override fun generateHash(value: String): String {
        return BCrypt.withDefaults().hashToString(12, value.toCharArray())
    }

    override fun verify(value: String, hash: String): Boolean {
        val result = BCrypt.verifyer().verify(value.toCharArray(), hash)
        return result.verified
    }
}