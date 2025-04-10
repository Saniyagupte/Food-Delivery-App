package com.foodie.admin.network

data class FoodItem (
    val item_name: String,
    val item_price: String,
    val item_description: String,
    val item_ingredients: String,
    val item_imageBase64: String
)

data class ApiResponse(
    val success: Boolean,
    val message: String
)