// LoginResponse.kt
package com.foodie.user.network

data class LoginResponse(
    val message: String,
    val user: User? = null
)
