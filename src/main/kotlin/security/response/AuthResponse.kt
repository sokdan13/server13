package com.example.security.response

import com.example.security.utils.ErrorCode
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse<T>(
    val success: Boolean,
    val errorCode: ErrorCode? = null,
    val data: T? = null
)