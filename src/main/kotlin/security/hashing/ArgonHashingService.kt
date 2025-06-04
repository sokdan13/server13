package com.example.security.hashing

import de.mkammerer.argon2.Argon2

class ArgonHashingService(val argon2: Argon2): HashingService {
    override fun generateHash(
        value: String
    ): String {
        return argon2.hash(3, 65536, 1, value.toCharArray())
    }

    override fun verify(value: String, hash : String): Boolean {
        return argon2.verify(hash, value.toCharArray())
    }
}