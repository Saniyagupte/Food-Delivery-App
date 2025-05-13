package com.foodie.user.model


data class FoodItem(
    val id: Int,
    val name: String,
    val price: Int,
    val description: String,
    val ingredients: String,
    val imageBase64: String,
)
