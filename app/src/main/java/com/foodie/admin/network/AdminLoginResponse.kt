// LoginResponse.kt
package com.foodie.admin.network

data class AdminLoginResponse(
    val message: String,
    val owner: Owner? = null
)
