package com.example.database.token


interface TokenDataSource {
    suspend fun insertToken(token: Token) : Boolean
    suspend fun findToken(refreshToken: String) : Token?
    suspend fun updateToken(token: Token) : Int
}