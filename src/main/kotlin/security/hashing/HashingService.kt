package com.example.security.hashing

interface HashingService {
    fun generateHash(value : String) : String
    fun verify(value: String, hash: String) : Boolean
}