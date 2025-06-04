package com.example.database.user

interface UserDataSource {
    suspend fun getUserByUsername(username: String): UserDTO?
    suspend fun insertUser(user: User) : Int?
    suspend fun userExists(username: String) : Boolean
}