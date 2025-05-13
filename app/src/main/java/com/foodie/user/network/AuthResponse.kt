package com.foodie.user.network

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val sessionToken: String?,
    val userId: Int? // Or the appropriate type for your user ID
)