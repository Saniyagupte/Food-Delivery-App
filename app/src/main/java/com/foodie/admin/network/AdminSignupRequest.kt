package com.foodie.admin.network

data class AdminSignupRequest (
    val owner_name: String,
    val restaurant_name: String,
    val email: String,
    val password: String,
    val location: String,
)