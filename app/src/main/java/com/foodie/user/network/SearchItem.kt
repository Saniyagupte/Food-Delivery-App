package com.foodie.user.network

data class SearchItem(
    val name: String,
    val description: String,
    val ingredients: String,
    val imageBase64: String,
    val ingredientImageUrls: List<String> // New field
)

