package com.foodie.user.network

data class SignupResponse(
    val message: String,
    val user: User? = null
)
